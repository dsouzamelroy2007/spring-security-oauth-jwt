package com.mel.expensetracker.resourceserver.item.dto;

import com.mel.expensetracker.resourceserver.item.ExpenseItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseItemResponse(
        UUID id, UUID categoryId, String categoryName, BigDecimal amount, String currency, LocalDate expenseDate, String note) {

    public static ExpenseItemResponse from(ExpenseItem item) {
        return new ExpenseItemResponse(
                item.getId(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getAmount(),
                item.getCurrency(),
                item.getExpenseDate(),
                item.getNote());
    }
}
