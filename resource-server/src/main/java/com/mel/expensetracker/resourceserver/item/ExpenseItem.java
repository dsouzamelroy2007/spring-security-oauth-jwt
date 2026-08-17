package com.mel.expensetracker.resourceserver.item;

import com.mel.expensetracker.resourceserver.category.Category;
import com.mel.expensetracker.resourceserver.report.ExpenseReport;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expense_items")
public class ExpenseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private ExpenseReport report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private BigDecimal amount;

    private String currency;

    private LocalDate expenseDate;

    private String note;

    private Instant createdAt;

    protected ExpenseItem() {}

    public ExpenseItem(ExpenseReport report, Category category, BigDecimal amount, String currency, LocalDate expenseDate, String note) {
        this.report = report;
        this.category = category;
        this.amount = amount;
        this.currency = currency;
        this.expenseDate = expenseDate;
        this.note = note;
    }

    public UUID getId() {
        return id;
    }

    public ExpenseReport getReport() {
        return report;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
