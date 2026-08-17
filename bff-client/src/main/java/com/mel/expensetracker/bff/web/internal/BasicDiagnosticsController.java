package com.mel.expensetracker.bff.web.internal;

import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** [FEATURE A2] Exists only so {@code BasicAuthSecurityConfig}'s chain has something real to protect. */
@RestController
public class BasicDiagnosticsController {

    @GetMapping("/internal/basic/whoami")
    public Map<String, String> whoAmI(Principal principal) {
        return Map.of("principal", principal.getName());
    }
}
