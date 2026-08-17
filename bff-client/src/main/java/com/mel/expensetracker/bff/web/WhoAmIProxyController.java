package com.mel.expensetracker.bff.web;

import com.mel.expensetracker.bff.relay.ResourceServerClient;
import com.mel.expensetracker.bff.relay.dto.WhoAmIResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** [FEATURE B1] SPA-facing endpoint, relayed to resource-server as the signed-in user. */
@RestController
public class WhoAmIProxyController {

    private final ResourceServerClient resourceServerClient;

    public WhoAmIProxyController(ResourceServerClient resourceServerClient) {
        this.resourceServerClient = resourceServerClient;
    }

    @GetMapping("/whoami")
    public WhoAmIResponse whoAmI() {
        return resourceServerClient.whoAmI();
    }
}
