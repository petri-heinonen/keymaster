package fi.pmh.keymaster.persistence;

import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "keys")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Key extends BaseEntity {
    @Column(name = "client_id", nullable = false)
    private String clientId;
    @Column(name = "key_id", nullable = false, unique = true)
    private String keyId;
    @Column(name = "use")
    private KeyUse use;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "is_published", nullable = false)
    private boolean isPublished;
    @Column(name = "jwk", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = RSAKeyConverter.class)
    private RSAKey jwk;

    public Key(String clientId, String keyId, RSAKey jwk, LocalDateTime createdAt, LocalDateTime expiresAt, KeyUse use) {
        this.clientId = clientId;
        this.keyId = keyId;
        this.expiresAt = expiresAt;
        this.isPublished = false;
        this.use = use;
        this.jwk = jwk;

        this.setCreatedAt(createdAt);
    }

    public boolean isSignatureKey() {
        return KeyUse.SIGNATURE.equals(this.use);
    }

    public boolean isEncryptionKey() {
        return KeyUse.ENCRYPTION.equals(this.use);
    }
}