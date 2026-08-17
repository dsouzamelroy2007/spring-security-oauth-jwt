package com.mel.expensetracker.resourceserver.report;

import com.mel.expensetracker.resourceserver.item.ExpenseItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expense_reports")
public class ExpenseReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String orgSlug;

    private String submitterSubject;

    @Enumerated(EnumType.STRING)
    private ExpenseReportStatus status;

    private String title;

    private String description;

    private String currency;

    private BigDecimal totalAmount;

    // [FEATURE] Optimistic locking (SPEC's CRUD requirement) -- concurrent
    // PUTs to the same report race on this column; the loser gets a 409/412
    // instead of silently overwriting the winner's change.
    @Version
    private long version;

    private Instant createdAt;

    private Instant updatedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt asc")
    private List<ExpenseItem> items = new ArrayList<>();

    protected ExpenseReport() {}

    public ExpenseReport(String orgSlug, String submitterSubject, String title, String description, String currency) {
        this.orgSlug = orgSlug;
        this.submitterSubject = submitterSubject;
        this.status = ExpenseReportStatus.DRAFT;
        this.title = title;
        this.description = description;
        this.currency = currency;
        this.totalAmount = BigDecimal.ZERO;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void updateDetails(String title, String description, String currency) {
        this.title = title;
        this.description = description;
        this.currency = currency;
    }

    public void submit() {
        this.status = ExpenseReportStatus.SUBMITTED;
    }

    public void approve() {
        this.status = ExpenseReportStatus.APPROVED;
    }

    public void addItem(ExpenseItem item) {
        items.add(item);
        this.totalAmount = this.totalAmount.add(item.getAmount());
    }

    public UUID getId() {
        return id;
    }

    public String getOrgSlug() {
        return orgSlug;
    }

    public String getSubmitterSubject() {
        return submitterSubject;
    }

    public ExpenseReportStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<ExpenseItem> getItems() {
        return items;
    }
}
