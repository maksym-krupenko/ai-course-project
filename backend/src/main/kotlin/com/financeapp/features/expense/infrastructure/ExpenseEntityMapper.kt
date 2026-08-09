package com.financeapp.features.expense.infrastructure

import com.financeapp.features.expense.domain.Category
import com.financeapp.features.expense.domain.Expense
import com.financeapp.features.expense.domain.Money

fun Expense.toEntity(): ExpenseEntity =
	ExpenseEntity(
		id = id,
		amount = amount.amount,
		currency = amount.currency,
		expenseDate = expenseDate,
		category = category.code,
		note = note,
	)

fun ExpenseEntity.toDomain(): Expense =
	Expense(
		id = id,
		amount = Money(amount = amount, currency = currency),
		expenseDate = expenseDate,
		category = Category.fromCode(category),
		note = note,
	)
