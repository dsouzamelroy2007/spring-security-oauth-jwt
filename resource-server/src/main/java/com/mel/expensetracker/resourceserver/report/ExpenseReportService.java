package com.mel.expensetracker.resourceserver.report;

import com.mel.expensetracker.resourceserver.category.Category;
import com.mel.expensetracker.resourceserver.category.CategoryRepository;
import com.mel.expensetracker.resourceserver.item.ExpenseItem;
import com.mel.expensetracker.resourceserver.report.dto.CreateExpenseReportRequest;
import com.mel.expensetracker.resourceserver.report.dto.UpdateExpenseReportRequest;
import com.mel.expensetracker.shared.org.OrgContext;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseReportService {

    private final ExpenseReportRepository reportRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseReportService(ExpenseReportRepository reportRepository, CategoryRepository categoryRepository) {
        this.reportRepository = reportRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    // [FEATURE C3] Collection filtering: reports across the org are tenant-
    // scoped by the query itself, but another user's DRAFT is still a private,
    // not-yet-submitted document -- @PostFilter drops those after the query,
    // leaving everyone's non-draft reports plus the caller's own drafts.
    @PostFilter("filterObject.status.name() != 'DRAFT' or filterObject.submitterSubject == authentication.name")
    public List<ExpenseReport> listReports(String orgSlug, Pageable pageable) {
        Page<ExpenseReport> page = reportRepository.findByOrgSlug(orgSlug, pageable);
        return new ArrayList<>(page.getContent());
    }

    @Transactional
    public ExpenseReport createReport(OrgContext caller, CreateExpenseReportRequest request) {
        ExpenseReport report = new ExpenseReport(
                caller.orgSlug(), caller.subject(), request.title(), request.description(), request.currency());
        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public ExpenseReport getReport(UUID id) {
        return findWithItemsOrThrow(id);
    }

    @Transactional
    public ExpenseReport updateReport(UUID id, UpdateExpenseReportRequest request, long expectedVersion) {
        ExpenseReport report = findWithItemsOrThrow(id);
        if (report.getVersion() != expectedVersion) {
            throw new VersionConflictException(expectedVersion, report.getVersion());
        }
        report.updateDetails(request.title(), request.description(), request.currency());
        return report;
    }

    @Transactional
    public ExpenseReport approveReport(UUID id) {
        ExpenseReport report = findWithItemsOrThrow(id);
        report.approve();
        return report;
    }

    @Transactional
    public void deleteReport(UUID id) {
        reportRepository.delete(findOrThrow(id));
    }

    @Transactional
    public ExpenseReport addItem(
            UUID reportId, UUID categoryId, java.math.BigDecimal amount, String currency, java.time.LocalDate expenseDate, String note) {
        ExpenseReport report = findWithItemsOrThrow(reportId);
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("No category with id " + categoryId));
        report.addItem(new ExpenseItem(report, category, amount, currency, expenseDate, note));
        return report;
    }

    private ExpenseReport findOrThrow(UUID id) {
        return reportRepository.findById(id).orElseThrow(() -> new ReportNotFoundException(id));
    }

    // Callers that return an ExpenseReportDetail (which serializes `items`)
    // need the collection initialized before the transaction closes --
    // open-in-view is off, so the controller can't lazy-load it afterward.
    private ExpenseReport findWithItemsOrThrow(UUID id) {
        return reportRepository.findByIdWithItems(id).orElseThrow(() -> new ReportNotFoundException(id));
    }
}
