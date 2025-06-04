package fi.pmh.keymaster.domain;

import lombok.Data;

@Data
public class Client {
    private String clientId;
    private String signatureKey;
    private String encryptionKey;
    private boolean isPublished;
    private String created;
    private String expires;
}
