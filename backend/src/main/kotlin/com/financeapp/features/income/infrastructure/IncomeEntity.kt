package com.financeapp.features.income.infrastructure

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "incomes")
class IncomeEntity(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,
	@Column(nullable = false)
	val amount: BigDecimal,
	@Column(nullable = false)
	val currency: String,
	@Column(name = "income_date", nullable = false)
	val incomeDate: LocalDate,
	@Column(nullable = false)
	val source: String,
	@Column
	val note: String?,
)
