package com.mel.expensetracker.authserver.web;

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * [FEATURE B10] Renders the scopes a client is requesting, split into
 * newly-requested vs. previously-approved, so a returning user only has to
 * re-approve what's new. Submitting this form posts back to /oauth2/authorize,
 * which Spring Authorization Server itself handles.
 *
 * <p>Adapted from Spring Authorization Server's own demo-authorizationserver
 * sample (AuthorizationConsentController), trimmed of the device-flow
 * (user_code) branch this project doesn't implement.
 */
@Controller
public class ConsentController {

    private static final Map<String, String> SCOPE_DESCRIPTIONS = Map.of(
            OidcScopes.PROFILE, "Read your name and account details.",
            "expenses.read", "Read your expense reports and reimbursements.",
            "expenses.write", "Submit and edit expense reports on your behalf.");

    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationConsentService authorizationConsentService;

    public ConsentController(
            RegisteredClientRepository registeredClientRepository,
            OAuth2AuthorizationConsentService authorizationConsentService) {
        this.registeredClientRepository = registeredClientRepository;
        this.authorizationConsentService = authorizationConsentService;
    }

    @GetMapping("/oauth2/consent")
    public String consent(
            Principal principal,
            Model model,
            @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
            @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
            @RequestParam(OAuth2ParameterNames.STATE) String state) {

        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        OAuth2AuthorizationConsent currentConsent =
                authorizationConsentService.findById(registeredClient.getId(), principal.getName());
        Set<String> alreadyApproved =
                currentConsent != null ? currentConsent.getScopes() : Set.of();

        Set<String> scopesToApprove = new HashSet<>();
        Set<String> previouslyApprovedScopes = new HashSet<>();
        for (String requestedScope : StringUtils.delimitedListToStringArray(scope, " ")) {
            if (OidcScopes.OPENID.equals(requestedScope)) {
                continue;
            }
            if (alreadyApproved.contains(requestedScope)) {
                previouslyApprovedScopes.add(requestedScope);
            } else {
                scopesToApprove.add(requestedScope);
            }
        }

        model.addAttribute("clientId", clientId);
        model.addAttribute("state", state);
        model.addAttribute("scopes", withDescription(scopesToApprove));
        model.addAttribute("previouslyApprovedScopes", withDescription(previouslyApprovedScopes));
        model.addAttribute("principalName", principal.getName());
        model.addAttribute("requestURI", "/oauth2/authorize");

        return "consent";
    }

    private static Set<ScopeWithDescription> withDescription(Set<String> scopes) {
        Set<ScopeWithDescription> result = new HashSet<>();
        for (String scope : scopes) {
            result.add(new ScopeWithDescription(scope, SCOPE_DESCRIPTIONS.getOrDefault(scope, scope)));
        }
        return result;
    }

    public record ScopeWithDescription(String scope, String description) {}
}
