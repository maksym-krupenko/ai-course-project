package com.financeapp.features.income

import com.financeapp.common.error.DomainValidationException
import com.financeapp.features.income.domain.Period
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertFailsWith

class PeriodTest {
	@Test
	fun `rejects a from date after the to date`() {
		assertFailsWith<DomainValidationException> {
			Period(from = LocalDate.of(2026, 1, 10), to = LocalDate.of(2026, 1, 1))
		}
	}

	@Test
	fun `accepts a single-day period where from equals to`() {
		val date = LocalDate.of(2026, 1, 1)

		val period = Period(from = date, to = date)

		assert(period.from == date)
		assert(period.to == date)
	}

	@Test
	fun `accepts an arbitrary non-calendar-month range`() {
		val period = Period(from = LocalDate.of(2026, 1, 15), to = LocalDate.of(2026, 2, 3))

		assert(period.from.isBefore(period.to))
	}
}
