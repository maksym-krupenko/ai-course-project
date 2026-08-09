package com.financeapp.features.expense.domain

import com.financeapp.common.error.DomainValidationException
import java.time.LocalDate

data class Period(
	val from: LocalDate,
	val to: LocalDate,
) {
	init {
		if (from.isAfter(to)) {
			throw DomainValidationException("Period 'from' ($from) must not be after 'to' ($to)")
		}
	}
}
