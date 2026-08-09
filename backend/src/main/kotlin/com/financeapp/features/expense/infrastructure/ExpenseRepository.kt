package com.financeapp.features.expense.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ExpenseRepository : JpaRepository<ExpenseEntity, Long> {
	fun findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(
		from: LocalDate,
		to: LocalDate,
	): List<ExpenseEntity>
}
