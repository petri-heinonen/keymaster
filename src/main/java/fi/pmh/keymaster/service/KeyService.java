package fi.pmh.keymaster.service;

import fi.pmh.keymaster.persistence.Key;

import java.util.List;

public interface KeyService {
    List<Key> getKeysByClientId(String clientId);
    List<Key> getPublishedKeysByClientId(String clientId);
    void publishKey(String keyId);
    void unpublishKey(String keyId);

    String publishMostRecentKeys(String clientId);

    List<String> getClientIds();

    void deleteAll();
}
