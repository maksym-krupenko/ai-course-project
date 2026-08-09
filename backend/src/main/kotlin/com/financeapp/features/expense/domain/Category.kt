package com.financeapp.features.expense.domain

import com.financeapp.common.error.DomainValidationException

sealed class Category(
	val code: String,
	val label: String,
) {
	data object Groceries : Category("GROCERIES", "Groceries")

	data object DiningOut : Category("DINING_OUT", "Dining Out")

	data object Transport : Category("TRANSPORT", "Transport")

	data object Housing : Category("HOUSING", "Housing")

	data object Utilities : Category("UTILITIES", "Utilities")

	data object Health : Category("HEALTH", "Health")

	data object Entertainment : Category("ENTERTAINMENT", "Entertainment")

	data object Shopping : Category("SHOPPING", "Shopping")

	data object Travel : Category("TRAVEL", "Travel")

	data object Education : Category("EDUCATION", "Education")

	data object PersonalCare : Category("PERSONAL_CARE", "Personal Care")

	data object Other : Category("OTHER", "Other")

	companion object {
		val all: List<Category> =
			listOf(
				Groceries,
				DiningOut,
				Transport,
				Housing,
				Utilities,
				Health,
				Entertainment,
				Shopping,
				Travel,
				Education,
				PersonalCare,
				Other,
			)

		fun fromCode(code: String): Category =
			all.find { it.code == code }
				?: throw DomainValidationException("Unknown category code: $code")
	}
}
