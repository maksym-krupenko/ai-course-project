package com.financeapp.features.expense.api

import com.financeapp.features.expense.application.ExpenseService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import java.math.BigDecimal
import java.time.LocalDate

data class CategoryPayload(
	val code: String,
	val label: String,
)

data class ExpensePayload(
	val id: String,
	val amount: BigDecimal,
	val currency: String,
	val expenseDate: LocalDate,
	val category: CategoryPayload,
	val note: String?,
)

data class RecordExpenseInput(
	val amount: BigDecimal,
	val expenseDate: LocalDate?,
	val categoryCode: String,
	val note: String?,
)

data class EditExpenseInput(
	val amount: BigDecimal,
	val expenseDate: LocalDate,
	val categoryCode: String,
	val note: String?,
)

@DgsComponent
class ExpenseDataFetcher(
	private val expenseService: ExpenseService,
) {
	@DgsQuery
	fun expenses(
		@InputArgument from: LocalDate,
		@InputArgument to: LocalDate,
	): List<ExpensePayload> =
		expenseService.findByPeriod(from, to).map { expense ->
			ExpensePayload(
				id = expense.id.toString(),
				amount = expense.amount.amount,
				currency = expense.amount.currency,
				expenseDate = expense.expenseDate,
				category = CategoryPayload(code = expense.category.code, label = expense.category.label),
				note = expense.note,
			)
		}

	@DgsQuery
	fun categories(): List<CategoryPayload> =
		expenseService.listCategories().map { category ->
			CategoryPayload(code = category.code, label = category.label)
		}

	@DgsMutation
	fun recordExpense(
		@InputArgument input: RecordExpenseInput,
	): ExpensePayload {
		val expense =
			expenseService.record(
				amount = input.amount,
				expenseDate = input.expenseDate,
				categoryCode = input.categoryCode,
				note = input.note,
			)
		return ExpensePayload(
			id = expense.id.toString(),
			amount = expense.amount.amount,
			currency = expense.amount.currency,
			expenseDate = expense.expenseDate,
			category = CategoryPayload(code = expense.category.code, label = expense.category.label),
			note = expense.note,
		)
	}

	@DgsMutation
	fun editExpense(
		@InputArgument id: String,
		@InputArgument input: EditExpenseInput,
	): ExpensePayload {
		val expense =
			expenseService.edit(
				id = id.toLong(),
				amount = input.amount,
				expenseDate = input.expenseDate,
				categoryCode = input.categoryCode,
				note = input.note,
			)
		return ExpensePayload(
			id = expense.id.toString(),
			amount = expense.amount.amount,
			currency = expense.amount.currency,
			expenseDate = expense.expenseDate,
			category = CategoryPayload(code = expense.category.code, label = expense.category.label),
			note = expense.note,
		)
	}

	@DgsMutation
	fun deleteExpense(
		@InputArgument id: String,
	): String {
		expenseService.delete(id.toLong())
		return id
	}
}
