package com.financeapp.features.income.domain

import java.time.LocalDate

data class Income(
	val id: Long? = null,
	val amount: Money,
	val incomeDate: LocalDate,
	val source: Source,
	val note: String?,
)
