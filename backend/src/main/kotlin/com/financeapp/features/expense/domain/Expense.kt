package com.financeapp.features.expense.domain

import java.time.LocalDate

data class Expense(
	val id: Long? = null,
	val amount: Money,
	val expenseDate: LocalDate,
	val category: Category,
	val note: String?,
)
