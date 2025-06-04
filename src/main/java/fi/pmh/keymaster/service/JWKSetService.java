package fi.pmh.keymaster.service;

import com.nimbusds.jose.jwk.JWKSet;
import fi.pmh.keymaster.domain.Client;
import fi.pmh.keymaster.persistence.Key;

import java.util.List;

public interface JWKSetService {
    JWKSet generateJwksSetFor(String clientId);
    JWKSet generateJwksSetFor(String clientId, boolean includeUnpublished);

    void deleteKeysForClient(String testClientId);
    List<Key> createKeysForClient(String testClientId);

    List<Client> getAllClientsWithKeys();

    void deleteKey(String keyId);

    void deleteAll();
}
