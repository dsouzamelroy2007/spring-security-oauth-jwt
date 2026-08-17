package com.mel.expensetracker.resourceserver.report;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseReportRepository extends JpaRepository<ExpenseReport, UUID> {

    Page<ExpenseReport> findByOrgSlug(String orgSlug, Pageable pageable);

    Optional<ExpenseReport> findByIdAndOrgSlug(UUID id, String orgSlug);

    // open-in-view is deliberately off: any caller that will read the lazy
    // `items` collection (and each item's lazy `category`) outside the
    // transaction -- i.e. anything ending up in an ExpenseReportDetail
    // response -- needs both fetched up front.
    @Query("select r from ExpenseReport r left join fetch r.items i left join fetch i.category where r.id = :id")
    Optional<ExpenseReport> findByIdWithItems(@Param("id") UUID id);
}
