package com.mel.expensetracker.resourceserver.item;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, UUID> {}
