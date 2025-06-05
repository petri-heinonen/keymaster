package fi.pmh.keymaster.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import fi.pmh.keymaster.config.ObjectMapperConfig;
import fi.pmh.keymaster.domain.Client;
import fi.pmh.keymaster.service.JWKSetService;
import fi.pmh.keymaster.service.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/public")
public class PublicController {
    @Autowired
    private JWKSetService jwkSetService;

    @Autowired
    private KeyService keyService;

    @Value("${settings.vault.key.lifetime}")
    private long keyLifetime;

    @Value("${settings.vault.key.create-before}")
    private long createBefore;

    @Value("${settings.vault.key.rotate-before}")
    private long rotateBefore;

    @GetMapping("/signed-jwks/{clientId}")
    public ResponseEntity<String> getSignedJwks(@PathVariable String clientId, @RequestParam(required = false) boolean all) throws JsonProcessingException {
        JWKSet set = jwkSetService.generateJwksSetFor(clientId, all);
        ObjectMapper objectMapper = ObjectMapperConfig.createObjectMapper();
        String jwksSet = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(set);
        return ResponseEntity.ok(jwksSet);
    }

    @GetMapping("/signed-jwks/all-clients")
    public ResponseEntity<?> getSignedJwksForAllClients() throws JsonProcessingException {
        List<String> clientIds = keyService.getClientIds();

        ObjectMapper objectMapper = ObjectMapperConfig.createObjectMapper();
        Map<String, String> jwksSets = clientIds.stream()
            .collect(Collectors.toMap(
                clientId -> clientId,
                clientId -> {
                    try {
                        JWKSet set = jwkSetService.generateJwksSetFor(clientId, false);
                        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(set);
                    } catch (JsonProcessingException e) {
                        return "Error generating JWKS for client: " + clientId;
                    }
                },
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ));

        return ResponseEntity.ok(jwksSets);
    }

    @GetMapping("/all-keys")
    public String getAllKeys(Model model) {
        model.addAttribute("configs", "Key lifetime: " + keyLifetime + " seconds<br>" +
            "Create before expiration: " + createBefore + " seconds<br>" +
            "Rotate before expiration: " + rotateBefore + " seconds");
        List<Client> clients = jwkSetService.getAllClientsWithKeys().stream().filter(Client::isPublished).collect(Collectors.toList());
        model.addAttribute("current", clients);
        List<Client> unpublished = jwkSetService.getAllClientsWithKeys().stream().filter(c -> !c.isPublished()).collect(Collectors.toList());
        model.addAttribute("unpublished", unpublished);
        return "clients-and-keys";
    }

    @GetMapping("/clients-and-keys")
    public ResponseEntity<?> getAllKeys() {
        List<Client> publishedClients = jwkSetService.getAllClientsWithKeys().stream()
            .filter(Client::isPublished)
            .toList();
        List<Client> unpublishedClients = jwkSetService.getAllClientsWithKeys().stream()
            .filter(c -> !c.isPublished())
            .toList();

        // Create a response object
        var response = Map.of(
        "current", publishedClients,
        "unpublished", unpublishedClients
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear-keys")
    public String clearKeys() {
        jwkSetService.deleteAll();
        return "redirect:/public/all-keys";
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody String body) {

        // Parse value from request body: clientId=00.client.fi
        String[] parts = body.split("=");
        if (parts.length != 2 || !parts[0].equals("clientId")) {
            return ResponseEntity.badRequest().body("Invalid clientId format. Expected format: clientId=your-client-id");
        }
        String clientId = parts[1].trim();
        jwkSetService.createKeysForClient(clientId);
        keyService.publishMostRecentKeys(clientId);

        return ResponseEntity.ok("Keys generated and published for client: " + clientId);
    }
}
