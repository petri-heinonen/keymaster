package fi.pmh.keymaster.service;

import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import fi.pmh.keymaster.config.YamlPropertySourceFactory;
import fi.pmh.keymaster.persistence.Key;
import fi.pmh.keymaster.persistence.KeyRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Data
@Service
@PropertySource(value = "classpath:settings.yaml", factory = YamlPropertySourceFactory.class)
public class KeyServiceImpl implements KeyService {

    private final KeyRepository keyRepository;

    @Value("${settings.vault.key.size}")
    private int keySize;

    @Value("${settings.vault.key.algorithm}")
    private String keyAlgorithm;

    @Value("${settings.vault.key.lifetime}")
    private long keyLifetime;

    @Autowired
    public KeyServiceImpl(KeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    public List<Key> getKeysByClientId(String clientId) {
        return keyRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    @Override
    public List<String> getClientIds() {
        return keyRepository.findAllClients();
    }

    public List<Key> getPublishedKeysByClientId(String clientId) {
        return keyRepository.findByClientIdAndIsPublishedTrueOrderByCreatedAtDesc(clientId);
    }

    @Override
    @Transactional
    public String publishMostRecentKeys(String clientId) {
        final List<Key> allKeys = getKeysByClientId(clientId);
        final List<Key> publishedKeys = getPublishedKeysByClientId(clientId);

        if (allKeys.isEmpty()) {
            log.warn("No keys found for client [{}]", clientId);
            return "No keys found for client: " + clientId;
        }

        if (allKeys.size() == publishedKeys.size()) {
            return "All keys for client [" + clientId + "] are already published.";
        }

        List<String> published = new ArrayList<String>();
        List<String> unpublished = new ArrayList<String>();

        // Publish the two most recent keys and unpublish the rest
        for (Key key : allKeys) {
            if (published.size() < 2) {
                published.add(key.getKeyId());
                publishKey(key.getKeyId());
            }
            else {
                unpublished.add(key.getKeyId());
                unpublishKey(key.getKeyId());
            }
        }

        return "Keys published for client [" + clientId + "]:\n" + Arrays.toString(published.toArray()) + "\nunpublished:\n" + Arrays.toString(unpublished.toArray());
    }

    // Publish a key
    @Transactional
    public void publishKey(String keyId) {
        Key key = keyRepository.findByKeyId(keyId);
        if (key == null) {
            log.warn("Key with ID {} not found for publishing", keyId);
        }
        else if (!key.isPublished()) {
            key.setPublished(true);
            keyRepository.save(key);
        }
    }

    // Unpublish a key
    @Transactional
    public void unpublishKey(String keyId) {
        Key key = keyRepository.findByKeyId(keyId);
        if (key == null) {
            log.warn("Key with ID {} not found for unpublishing", keyId);
        }
        else if (key.isPublished()) {
            key.setPublished(false);
            keyRepository.save(key);
        }
    }

    @Transactional
    public void deleteAll()
    {
        keyRepository.deleteAll();
    }

    @Transactional
    protected Key createKeyForClient(@NonNull String clientId, KeyUse use, LocalDateTime now) throws Exception {
        LocalDateTime expiresAt = now.plusSeconds(keyLifetime);
        Key key = createKey(clientId, use, now, expiresAt);
        return keyRepository.save(key);
    }

    @Transactional
    protected void deleteKey(String keyId) {
        Key key = keyRepository.findByKeyId(keyId);
        if (key != null) {
            keyRepository.delete(key);
        } else {
            log.warn("Key with ID {} not found for deletion", keyId);
        }
    }

    private Key createKey(String clientId, KeyUse use, LocalDateTime created, LocalDateTime expires) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm);
        keyPairGenerator.initialize(keySize);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .keyUse(use)
                .build();

        return new Key(clientId, jwk.getKeyID(), jwk, created, expires, use);
    }
}