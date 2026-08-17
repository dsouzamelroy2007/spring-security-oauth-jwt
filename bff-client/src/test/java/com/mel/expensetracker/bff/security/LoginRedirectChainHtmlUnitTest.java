package com.mel.expensetracker.bff.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.mel.expensetracker.bff.support.AbstractBffIntegrationTest;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;

/**
 * [FEATURE B1] Acceptance criterion 4: an HtmlUnit-driven check of the
 * redirect chain bff-client itself is responsible for -- clicking "Log in"
 * on the unauthenticated home page correctly initiates Spring's own
 * code+PKCE authorization request, landing on authorization-server's
 * {@code /oauth2/authorize} with the right query parameters.
 *
 * <p>Stops there rather than completing a real login: that needs
 * authorization-server booted alongside bff-client in the same test run --
 * the same dual-context pattern {@code integration-tests}' own
 * {@code CrossModuleTestSupport} already solves for M3's cross-module
 * tests. M4 deliberately doesn't re-derive that (a real, hard-won,
 * module-boundary-crossing setup) a second time here; the full round trip
 * is {@code integration-tests}' job (M5).
 */
class LoginRedirectChainHtmlUnitTest extends AbstractBffIntegrationTest {

    @Test
    void loginLinkInitiatesTheCodePkceAuthorizationRequest() throws Exception {
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setRedirectEnabled(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage homePage = webClient.getPage("http://localhost:" + port + "/");
            HtmlAnchor loginLink = homePage.getAnchorByHref("/oauth2/authorization/bff-client");

            Page redirectPage = loginLink.click();
            WebResponse response = redirectPage.getWebResponse();

            assertThat(response.getStatusCode()).isEqualTo(302);
            String location = response.getResponseHeaderValue("Location");
            assertThat(location).startsWith("http://localhost:9000/oauth2/authorize");
            assertThat(location).contains("client_id=bff-client");
            assertThat(location).contains("code_challenge_method=S256");
            assertThat(location).contains("response_type=code");
        }
    }
}
