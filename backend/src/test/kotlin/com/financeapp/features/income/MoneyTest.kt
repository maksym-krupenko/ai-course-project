package com.financeapp.features.income

import com.financeapp.common.error.DomainValidationException
import com.financeapp.features.income.domain.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertFailsWith

class MoneyTest {
	@Test
	fun `rejects zero amount`() {
		assertFailsWith<DomainValidationException> {
			Money(amount = BigDecimal.ZERO)
		}
	}

	@Test
	fun `rejects negative amount`() {
		assertFailsWith<DomainValidationException> {
			Money(amount = BigDecimal("-1.00"))
		}
	}

	@Test
	fun `rejects non-PLN currency`() {
		assertFailsWith<DomainValidationException> {
			Money(amount = BigDecimal("10.00"), currency = "USD")
		}
	}

	@Test
	fun `accepts a positive PLN amount`() {
		val money = Money(amount = BigDecimal("10.00"))

		assert(money.amount == BigDecimal("10.00"))
		assert(money.currency == "PLN")
	}
}
