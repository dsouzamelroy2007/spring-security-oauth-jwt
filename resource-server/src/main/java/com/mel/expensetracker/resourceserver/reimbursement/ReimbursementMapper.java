package com.mel.expensetracker.resourceserver.reimbursement;

import com.mel.expensetracker.resourceserver.reimbursement.dto.ReimbursementResponse;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * [FEATURE C5] Imperative masking, not {@code @PreAuthorize} on a getter --
 * a non-FINANCE caller should see the reimbursement with a masked IBAN, not
 * get a 403 for the whole record. Expands the caller's authorities through
 * the role hierarchy (MethodSecurityConfig) so {@code ORG_ADMIN} -- which
 * implies {@code FINANCE} -- also sees the real IBAN, reusing C2's hierarchy
 * outside of a SpEL expression.
 */
@Component
public class ReimbursementMapper {

    private static final String ROLE_FINANCE = "ROLE_FINANCE";

    private final RoleHierarchy roleHierarchy;

    public ReimbursementMapper(RoleHierarchy roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
    }

    public ReimbursementResponse toResponse(Reimbursement reimbursement, Authentication authentication) {
        String iban = canSeeRealIban(authentication) ? reimbursement.getIban() : IbanMasker.mask(reimbursement.getIban());
        return new ReimbursementResponse(
                reimbursement.getId(), reimbursement.getOrgSlug(), iban, reimbursement.getAmount(), reimbursement.getCurrency(), reimbursement.getPaidAt());
    }

    private boolean canSeeRealIban(Authentication authentication) {
        return roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities()).stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLE_FINANCE::equals);
    }
}
