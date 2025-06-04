package fi.pmh.keymaster.vault;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import fi.pmh.keymaster.config.ObjectMapperConfig;
import fi.pmh.keymaster.persistence.Key;
import fi.pmh.keymaster.service.JWKSetService;
import fi.pmh.keymaster.service.KeyService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
public class KeyServiceTest {
    private static final String TEST_CLIENT_ID = "test-client-id";
    private ObjectMapper objectMapper;

    @Autowired
    private KeyService keyService;

    @Autowired
    private JWKSetService jwksService;

    @BeforeEach
    public void setup() {
        objectMapper = ObjectMapperConfig.createObjectMapper();
        jwksService.deleteKeysForClient(TEST_CLIENT_ID);
    }

    @Test
    @Order(1)
    public void testGenerateNewKey() throws Exception {
        jwksService.deleteKeysForClient(TEST_CLIENT_ID);
        jwksService.createKeysForClient(TEST_CLIENT_ID);
        List<Key> keys = keyService.getKeysByClientId(TEST_CLIENT_ID);
        List<Key> publishedKeys = keyService.getPublishedKeysByClientId(TEST_CLIENT_ID);

        // Verify that keys are created
        assertEquals(2, keys.size());
        // Newly generated keys are not published by default
        assertEquals(0, publishedKeys.size());
    }

    @Test
    @Order(2)
    public void testPublishKey() throws Exception {
        jwksService.createKeysForClient(TEST_CLIENT_ID);
        List<Key> keys = keyService.getKeysByClientId(TEST_CLIENT_ID);
        for (Key key : keys) {
            keyService.publishKey(key.getKeyId());
        }
        List<Key> publishedKeys = keyService.getPublishedKeysByClientId(TEST_CLIENT_ID);
        assertEquals(2, publishedKeys.size());
    }

    @Test
    @Order(3)
    public void testGenerateJwksSet() throws Exception {
        jwksService.createKeysForClient(TEST_CLIENT_ID);

        // Get all keys for the client
        List<Key> keys = keyService.getKeysByClientId(TEST_CLIENT_ID);
        assertEquals(2, keys.size());

        // Publish keys
        List<Key> publishedKeys = keyService.getKeysByClientId(TEST_CLIENT_ID);
        for (Key key : publishedKeys) {
            keyService.publishKey(key.getKeyId());
        }

        JWKSet set = jwksService.generateJwksSetFor(TEST_CLIENT_ID);
        String jwksSet = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(set);

        log.info("signed-jwks:\n{}", jwksSet);

        assertEquals(2, set.size());
        assertTrue(jwksSet.contains(publishedKeys.get(0).getKeyId()));
        assertTrue(jwksSet.contains(publishedKeys.get(1).getKeyId()));
    }
}