package com.financeapp.features.income.domain

import com.financeapp.common.error.DomainValidationException

sealed class Source(
	val code: String,
	val label: String,
) {
	data object Salary : Source("SALARY", "Salary")

	data object FreelanceSideIncome : Source("FREELANCE_SIDE_INCOME", "Freelance / Side Income")

	data object Gift : Source("GIFT", "Gift")

	data object Refund : Source("REFUND", "Refund")

	data object InvestmentReturn : Source("INVESTMENT_RETURN", "Investment Return")

	data object Other : Source("OTHER", "Other")

	companion object {
		val all: List<Source> =
			listOf(
				Salary,
				FreelanceSideIncome,
				Gift,
				Refund,
				InvestmentReturn,
				Other,
			)

		fun fromCode(code: String): Source =
			all.find { it.code == code }
				?: throw DomainValidationException("Unknown source code: $code")
	}
}
