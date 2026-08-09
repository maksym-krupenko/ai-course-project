package com.financeapp.features.expense

import com.financeapp.common.error.DomainValidationException
import com.financeapp.common.error.NotFoundException
import com.financeapp.features.expense.application.ExpenseService
import com.financeapp.features.expense.infrastructure.ExpenseEntity
import com.financeapp.features.expense.infrastructure.ExpenseRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExpenseServiceTest {
	@Test
	fun `record defaults the expense date to today when none is given`() {
		val repository = mockk<ExpenseRepository>()
		val savedSlot = slot<ExpenseEntity>()
		every { repository.save(capture(savedSlot)) } answers { savedSlot.captured }
		val service = ExpenseService(repository)

		val result = service.record(amount = BigDecimal("12.50"), expenseDate = null, categoryCode = "GROCERIES", note = null)

		assertEquals(LocalDate.now(), result.expenseDate)
		verify { repository.save(any()) }
	}

	@Test
	fun `record propagates DomainValidationException for a non-positive amount`() {
		val repository = mockk<ExpenseRepository>()
		val service = ExpenseService(repository)

		assertFailsWith<DomainValidationException> {
			service.record(amount = BigDecimal.ZERO, expenseDate = null, categoryCode = "GROCERIES", note = null)
		}
	}

	@Test
	fun `record propagates DomainValidationException for an unknown category`() {
		val repository = mockk<ExpenseRepository>()
		val service = ExpenseService(repository)

		assertFailsWith<DomainValidationException> {
			service.record(amount = BigDecimal("10.00"), expenseDate = null, categoryCode = "NOT_A_CATEGORY", note = null)
		}
	}

	@Test
	fun `edit throws NotFoundException when the id does not exist`() {
		val repository = mockk<ExpenseRepository> { every { existsById(99L) } returns false }
		val service = ExpenseService(repository)

		assertFailsWith<NotFoundException> {
			service.edit(id = 99L, amount = BigDecimal("10.00"), expenseDate = LocalDate.now(), categoryCode = "GROCERIES", note = null)
		}
	}

	@Test
	fun `delete throws NotFoundException when the id does not exist`() {
		val repository = mockk<ExpenseRepository> { every { existsById(99L) } returns false }
		val service = ExpenseService(repository)

		assertFailsWith<NotFoundException> {
			service.delete(99L)
		}
	}
}
