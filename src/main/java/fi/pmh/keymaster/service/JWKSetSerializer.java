package fi.pmh.keymaster.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.nimbusds.jose.jwk.JWKSet;

import java.io.IOException;

@JsonSerialize(using = JWKSetSerializer.class)
public class JWKSetSerializer extends StdSerializer<JWKSet> {

    public JWKSetSerializer() {
        super(JWKSet.class);
    }

    @Override
    public void serialize(JWKSet value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        value.toJSONObject().forEach((key, val) -> {
            try {
                if (val != null && !val.toString().isEmpty()) {
                    gen.writeObjectField(key, val);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        gen.writeEndObject();
    }
}