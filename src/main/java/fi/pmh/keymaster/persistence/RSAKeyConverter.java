package fi.pmh.keymaster.persistence;

import com.nimbusds.jose.jwk.RSAKey;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RSAKeyConverter implements AttributeConverter<RSAKey, String> {

    @Override
    public String convertToDatabaseColumn(RSAKey rsaKey) {
        return rsaKey != null ? rsaKey.toJSONString() : null;
    }

    @Override
    public RSAKey convertToEntityAttribute(String dbData) {
        try {
            return dbData != null ? RSAKey.parse(dbData) : null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse RSAKey from database", e);
        }
    }
}