package com.mel.expensetracker.resourceserver.reimbursement;

import com.mel.expensetracker.resourceserver.reimbursement.dto.ReimbursementResponse;
import com.mel.expensetracker.shared.org.OrgContext;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** [style: field redaction] Tenant-scoped; IBAN masked unless the caller effectively holds ROLE_FINANCE. */
@RestController
public class ReimbursementController {

    private final ReimbursementRepository reimbursementRepository;
    private final ReimbursementMapper mapper;

    public ReimbursementController(ReimbursementRepository reimbursementRepository, ReimbursementMapper mapper) {
        this.reimbursementRepository = reimbursementRepository;
        this.mapper = mapper;
    }

    @GetMapping("/api/v1/reimbursements")
    @PreAuthorize("isAuthenticated()")
    public List<ReimbursementResponse> listReimbursements(OrgContext caller, Authentication authentication, Pageable pageable) {
        return reimbursementRepository.findByOrgSlug(caller.orgSlug(), pageable).getContent().stream()
                .map(reimbursement -> mapper.toResponse(reimbursement, authentication))
                .toList();
    }
}
