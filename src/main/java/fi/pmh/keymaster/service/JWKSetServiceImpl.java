package fi.pmh.keymaster.service;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import fi.pmh.keymaster.domain.Client;
import fi.pmh.keymaster.persistence.Key;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class JWKSetServiceImpl implements JWKSetService {
    private final KeyServiceImpl keyService;

    public JWKSet generateJwksSetFor(String clientId) {
        return generateJwksSetFor(clientId, false);
    }

    public JWKSet generateJwksSetFor(String clientId, boolean includeUnpublished) {
        List<Key> publishedKeys = includeUnpublished ? keyService.getKeysByClientId(clientId) : keyService.getPublishedKeysByClientId(clientId);
        List<JWK> jwkList = publishedKeys.stream().map(key -> (JWK) key.getJwk()).toList();

        Key firstPublishedKey = publishedKeys.stream().filter(Key::isPublished).findFirst().orElse(null);

        JWKSet jwkSet = null;

        if (firstPublishedKey != null) {
            Map<String, Object> properties = Map.of(
                "iss", clientId,
                "sub", clientId,
                "iat", String.valueOf(firstPublishedKey.getCreatedAt().toEpochSecond(ZoneOffset.UTC)),
                "exp", String.valueOf(firstPublishedKey.getExpiresAt().toEpochSecond(ZoneOffset.UTC))
            );

            jwkSet = new JWKSet(jwkList, properties);
        }
        else {
            jwkSet = new JWKSet(jwkList);
        }

        return jwkSet;
    }

    public List<Client> getAllClientsWithKeys() {
        List<String> clientIds = keyService.getClientIds();
        List<Client> clients = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        for (String clientId : clientIds) {
            List<Key> keys = keyService.getKeysByClientId(clientId);

            for (int i = 0; i < keys.size(); i += 2) {
                Client client = new Client();
                client.setClientId(clientId);

                if (i < keys.size()) {
                    Key key1 = keys.get(i);
                    client.setPublished(key1.isPublished());
                    client.setCreated(key1.getCreatedAt().format(formatter));
                    client.setExpires(key1.getExpiresAt().format(formatter));
                    if (key1.isSignatureKey()) {
                        client.setSignatureKey(key1.getKeyId());
                    } else if (key1.isEncryptionKey()) {
                        client.setEncryptionKey(key1.getKeyId());
                    }
                }

                if (i + 1 < keys.size()) {
                    Key key2 = keys.get(i + 1);
                    if (key2.isSignatureKey()) {
                        client.setSignatureKey(key2.getKeyId());
                    } else if (key2.isEncryptionKey()) {
                        client.setEncryptionKey(key2.getKeyId());
                    }
                }

                if (client.getSignatureKey() == null || client.getEncryptionKey() == null) {
                    continue; // Skip clients without keys
                }

                clients.add(client);
            }
        }

        return clients;
    }

    public void deleteKeysForClient(String clientId) {
        List<Key> keys = keyService.getKeysByClientId(clientId);
        if (!keys.isEmpty()) {
            keys.forEach(key -> keyService.deleteKey(key.getKeyId()));
        }
    }

    @Override
    public void deleteKey(String keyId) {
        keyService.deleteKey(keyId);
    }

    @Override
    public void deleteAll() {
        keyService.deleteAll();
    }

    public List<Key> createKeysForClient(String clientId) {
        try
        {
            LocalDateTime now = LocalDateTime.now().withNano(0);
            Key sig = keyService.createKeyForClient(clientId, KeyUse.SIGNATURE, now);
            Key enc = keyService.createKeyForClient(clientId, KeyUse.ENCRYPTION, now);

            return List.of(sig, enc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create keys for client: " + clientId, e);
        }
    }
}
