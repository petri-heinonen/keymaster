package fi.pmh.keymaster.scheduler;

import fi.pmh.keymaster.config.YamlPropertySourceFactory;
import fi.pmh.keymaster.persistence.Key;
import fi.pmh.keymaster.service.JWKSetService;
import fi.pmh.keymaster.service.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@PropertySource(value = "classpath:settings.yaml", factory = YamlPropertySourceFactory.class)
public class KeyRotationScheduler {

    @Autowired
    private KeyService keyService;

    @Autowired
    private JWKSetService jwkSetService;

    @Value("${settings.vault.key.lifetime}")
    private long keyLifetime;

    @Value("${settings.vault.key.create-before}")
    private long createBefore;

    @Value("${settings.vault.key.rotate-before}")
    private long rotateBefore;

    @Scheduled(cron = "*/1 * * * * *")
    public void rotateKeys() {
        List<String> clients = keyService.getClientIds();
        LocalDateTime now = LocalDateTime.now();

        for (String clientId : clients) {
            List<Key> keys = keyService.getKeysByClientId(clientId);
            if (!keys.isEmpty()) {
                Key mostRecentKey = keys.get(0);
                if (mostRecentKey.getExpiresAt().isBefore(now.plusSeconds(createBefore))) {
                    jwkSetService.createKeysForClient(clientId);
                }
            }
        }

        for (String clientId : clients) {
            List<Key> keys = keyService.getKeysByClientId(clientId);
            if (!keys.isEmpty() && !keys.get(0).isPublished()) {
                Key mostRecentKey = keys.get(0);
                if (mostRecentKey.getCreatedAt().isBefore(now.minusSeconds(rotateBefore))) {
                    keyService.publishMostRecentKeys(clientId);
                }
            }
        }

        for (String clientId : clients) {
            List<Key> keys = keyService.getKeysByClientId(clientId);
            if (!keys.isEmpty()) {
                for (Key key : keys) {
                    if (key.getExpiresAt().isBefore(now.minusSeconds(keyLifetime))) {
                        jwkSetService.deleteKey(key.getKeyId());
                    }
                }
            }
        }
    }
}