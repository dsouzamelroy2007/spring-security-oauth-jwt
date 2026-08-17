package com.mel.expensetracker.resourceserver.reimbursement;

import com.mel.expensetracker.resourceserver.report.ExpenseReport;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Holds the payout IBAN -- the field-level redaction showcase (feature C5).
 * See {@link com.mel.expensetracker.resourceserver.reimbursement.IbanMasker}
 * for the threat this guards against.
 */
@Entity
@Table(name = "reimbursements")
public class Reimbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false, unique = true)
    private ExpenseReport report;

    private String orgSlug;

    private String iban;

    private BigDecimal amount;

    private String currency;

    private Instant paidAt;

    protected Reimbursement() {}

    public UUID getId() {
        return id;
    }

    public ExpenseReport getReport() {
        return report;
    }

    public String getOrgSlug() {
        return orgSlug;
    }

    public String getIban() {
        return iban;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getPaidAt() {
        return paidAt;
    }
}
