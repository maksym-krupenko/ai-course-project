package com.financeapp.features.expense.application

import com.financeapp.common.error.NotFoundException
import com.financeapp.features.expense.domain.Category
import com.financeapp.features.expense.domain.Expense
import com.financeapp.features.expense.domain.Money
import com.financeapp.features.expense.domain.Period
import com.financeapp.features.expense.infrastructure.ExpenseRepository
import com.financeapp.features.expense.infrastructure.toDomain
import com.financeapp.features.expense.infrastructure.toEntity
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class ExpenseService(
	private val expenseRepository: ExpenseRepository,
) {
	fun record(
		amount: BigDecimal,
		expenseDate: LocalDate?,
		categoryCode: String,
		note: String?,
	): Expense {
		val expense =
			Expense(
				amount = Money(amount = amount),
				expenseDate = expenseDate ?: LocalDate.now(),
				category = Category.fromCode(categoryCode),
				note = note,
			)
		return expenseRepository.save(expense.toEntity()).toDomain()
	}

	fun edit(
		id: Long,
		amount: BigDecimal,
		expenseDate: LocalDate,
		categoryCode: String,
		note: String?,
	): Expense {
		if (!expenseRepository.existsById(id)) {
			throw NotFoundException("No expense found with id $id")
		}
		val expense =
			Expense(
				id = id,
				amount = Money(amount = amount),
				expenseDate = expenseDate,
				category = Category.fromCode(categoryCode),
				note = note,
			)
		return expenseRepository.save(expense.toEntity()).toDomain()
	}

	fun delete(id: Long) {
		if (!expenseRepository.existsById(id)) {
			throw NotFoundException("No expense found with id $id")
		}
		expenseRepository.deleteById(id)
	}

	fun findByPeriod(
		from: LocalDate,
		to: LocalDate,
	): List<Expense> {
		val period = Period(from = from, to = to)
		return expenseRepository
			.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(period.from, period.to)
			.map { it.toDomain() }
	}

	fun listCategories(): List<Category> = Category.all
}
