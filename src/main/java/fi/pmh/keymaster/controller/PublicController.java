package fi.pmh.keymaster.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import fi.pmh.keymaster.config.ObjectMapperConfig;
import fi.pmh.keymaster.domain.Client;
import fi.pmh.keymaster.service.JWKSetService;
import fi.pmh.keymaster.service.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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

    private SseEmitter emitter;
    private boolean emitterCompleted = false;

    @GetMapping("/signed-jwks/{clientId}")
    public ResponseEntity<String> getSignedJwks(@PathVariable String clientId, @RequestParam(required = false) boolean all) throws JsonProcessingException {
        JWKSet set = jwkSetService.generateJwksSetFor(clientId, all);
        ObjectMapper objectMapper = ObjectMapperConfig.createObjectMapper();
        String jwksSet = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(set);
        return ResponseEntity.ok(jwksSet);
    }

    @GetMapping("/all-keys")
    public String getAllKeys(Model model) throws JsonProcessingException {
        List<Client> clients = jwkSetService.getAllClientsWithKeys().stream().filter(Client::isPublished).collect(Collectors.toList());
        model.addAttribute("current", clients);
        List<Client> unpublished = jwkSetService.getAllClientsWithKeys().stream().filter(c -> !c.isPublished()).collect(Collectors.toList());
        model.addAttribute("unpublished", unpublished);
        return "clients-and-keys";
    }

    @GetMapping("/clients-and-keys")
    public ResponseEntity<?> getAllKeys() throws JsonProcessingException {
        List<Client> publishedClients = jwkSetService.getAllClientsWithKeys().stream()
            .filter(Client::isPublished)
            .collect(Collectors.toList());
        List<Client> unpublishedClients = jwkSetService.getAllClientsWithKeys().stream()
            .filter(c -> !c.isPublished())
            .collect(Collectors.toList());

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
    public String generate(Model model, @RequestBody String body) {

        // Parse value from request body: clientId=00.client.fi
        String[] parts = body.split("=");
        if (parts.length != 2 || !parts[0].equals("clientId")) {
            model.addAttribute("error", "Invalid clientId format.");
            return "error";
        }
        String clientId = parts[1].trim();
        jwkSetService.createKeysForClient(clientId);
        keyService.publishMostRecentKeys(clientId);

        return "redirect:/public/all-keys";
    }
}
