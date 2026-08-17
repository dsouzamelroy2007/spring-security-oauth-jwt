package com.mel.expensetracker.resourceserver.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.mel.expensetracker.shared.authz.IsOrgAdmin;
import com.mel.expensetracker.shared.authz.PublicEndpoint;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [FEATURE] Fails loudly if a new endpoint is added (or an existing one's
 * annotation is deleted) without an explicit authorization decision -- every
 * {@code @RestController} request-mapped method must carry a real check
 * ({@code @PreAuthorize}, {@code @PostAuthorize}, {@code @IsOrgAdmin}) or be
 * explicitly marked {@code @PublicEndpoint}. URL-level checks in
 * {@code SecurityConfig} still do the real enforcement for several of these
 * (ownership/tenancy via {@code ReportAccessAuthorizationManager}); the
 * method-level annotation is the belt-and-suspenders half that ArchUnit can
 * actually see and verify mechanically.
 */
@AnalyzeClasses(packages = "com.mel.expensetracker.resourceserver", importOptions = ImportOption.DoNotIncludeTests.class)
class ControllerAuthorizationArchTest {

    @ArchTest
    static final ArchRule every_request_mapped_method_has_an_authorization_decision = methods()
            .that()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class)
            .and()
            // @GetMapping/@PostMapping/etc. are themselves meta-annotated with
            // @RequestMapping -- a direct areAnnotatedWith(RequestMapping.class)
            // would match none of them and vacuously pass every method.
            .areMetaAnnotatedWith(RequestMapping.class)
            .should(carryAnAuthorizationDecision());

    private static ArchCondition<JavaMethod> carryAnAuthorizationDecision() {
        return new ArchCondition<>("be annotated with @PreAuthorize, @PostAuthorize, @IsOrgAdmin, or @PublicEndpoint") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                boolean hasDecision = method.isAnnotatedWith(PreAuthorize.class)
                        || method.isAnnotatedWith(PostAuthorize.class)
                        || method.isAnnotatedWith(IsOrgAdmin.class)
                        || method.isAnnotatedWith(PublicEndpoint.class);
                if (!hasDecision) {
                    events.add(SimpleConditionEvent.violated(
                            method, method.getFullName() + " has no authorization annotation and is not marked @PublicEndpoint"));
                }
            }
        };
    }
}
