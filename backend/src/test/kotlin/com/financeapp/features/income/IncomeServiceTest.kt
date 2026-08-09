package com.financeapp.features.income

import com.financeapp.common.error.DomainValidationException
import com.financeapp.common.error.NotFoundException
import com.financeapp.features.income.application.IncomeService
import com.financeapp.features.income.infrastructure.IncomeEntity
import com.financeapp.features.income.infrastructure.IncomeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IncomeServiceTest {
	@Test
	fun `record defaults the income date to today when none is given`() {
		val repository = mockk<IncomeRepository>()
		val savedSlot = slot<IncomeEntity>()
		every { repository.save(capture(savedSlot)) } answers { savedSlot.captured }
		val service = IncomeService(repository)

		val result = service.record(amount = BigDecimal("12.50"), incomeDate = null, sourceCode = "SALARY", note = null)

		assertEquals(LocalDate.now(), result.incomeDate)
		verify { repository.save(any()) }
	}

	@Test
	fun `record propagates DomainValidationException for a non-positive amount`() {
		val repository = mockk<IncomeRepository>()
		val service = IncomeService(repository)

		assertFailsWith<DomainValidationException> {
			service.record(amount = BigDecimal.ZERO, incomeDate = null, sourceCode = "SALARY", note = null)
		}
	}

	@Test
	fun `record propagates DomainValidationException for an unknown source`() {
		val repository = mockk<IncomeRepository>()
		val service = IncomeService(repository)

		assertFailsWith<DomainValidationException> {
			service.record(amount = BigDecimal("10.00"), incomeDate = null, sourceCode = "NOT_A_SOURCE", note = null)
		}
	}

	@Test
	fun `edit throws NotFoundException when the id does not exist`() {
		val repository = mockk<IncomeRepository> { every { existsById(99L) } returns false }
		val service = IncomeService(repository)

		assertFailsWith<NotFoundException> {
			service.edit(id = 99L, amount = BigDecimal("10.00"), incomeDate = LocalDate.now(), sourceCode = "SALARY", note = null)
		}
	}

	@Test
	fun `delete throws NotFoundException when the id does not exist`() {
		val repository = mockk<IncomeRepository> { every { existsById(99L) } returns false }
		val service = IncomeService(repository)

		assertFailsWith<NotFoundException> {
			service.delete(99L)
		}
	}
}
