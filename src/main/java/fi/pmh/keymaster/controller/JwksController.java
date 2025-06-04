package fi.pmh.keymaster.controller;

import fi.pmh.keymaster.service.JWKSetService;
import fi.pmh.keymaster.service.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/jwks")
public class JwksController {
    @Autowired
    private JWKSetService jwkSetService;

    @Autowired
    private KeyService keyService;

    // Generate new keys for a client
    @PostMapping("/generate")
    public ResponseEntity<String> generateKeys(@RequestParam String clientId) {
        jwkSetService.createKeysForClient(clientId);
        return ResponseEntity.ok("Keys generated for client: " + clientId);
    }

    // Publish client keys
    @PostMapping("/publish")
    public ResponseEntity<String> publishKeys(@RequestParam String clientId) {
        String response = keyService.publishMostRecentKeys(clientId);
        return ResponseEntity.ok(response);
    }
}
