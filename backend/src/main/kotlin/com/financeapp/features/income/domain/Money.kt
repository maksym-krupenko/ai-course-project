package com.financeapp.features.income.domain

import com.financeapp.common.error.DomainValidationException
import java.math.BigDecimal

data class Money(
	val amount: BigDecimal,
	val currency: String = "PLN",
) {
	init {
		if (amount <= BigDecimal.ZERO) {
			throw DomainValidationException("Amount must be greater than zero, was $amount")
		}
		if (currency != "PLN") {
			throw DomainValidationException("Currency must be PLN, was $currency")
		}
	}
}
