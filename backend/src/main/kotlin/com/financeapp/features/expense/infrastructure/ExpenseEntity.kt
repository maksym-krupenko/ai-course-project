package com.financeapp.features.expense.infrastructure

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "expenses")
class ExpenseEntity(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,
	@Column(nullable = false)
	val amount: BigDecimal,
	@Column(nullable = false)
	val currency: String,
	@Column(name = "expense_date", nullable = false)
	val expenseDate: LocalDate,
	@Column(nullable = false)
	val category: String,
	@Column
	val note: String?,
)
