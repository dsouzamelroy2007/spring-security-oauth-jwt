package com.mel.expensetracker.bff.web.internal;

import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** [FEATURE A5] Exists only so {@code ApiKeySecurityConfig}'s chain has something real to protect. */
@RestController
public class ApiKeyDiagnosticsController {

    @GetMapping("/internal/api-key/whoami")
    public Map<String, String> whoAmI(Principal principal) {
        return Map.of("principal", principal.getName());
    }
}
