package com.financeapp.features.income.api

import com.financeapp.features.income.application.IncomeService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import java.math.BigDecimal
import java.time.LocalDate

data class SourcePayload(
	val code: String,
	val label: String,
)

data class IncomePayload(
	val id: String,
	val amount: BigDecimal,
	val currency: String,
	val incomeDate: LocalDate,
	val source: SourcePayload,
	val note: String?,
)

data class RecordIncomeInput(
	val amount: BigDecimal,
	val incomeDate: LocalDate?,
	val sourceCode: String,
	val note: String?,
)

data class EditIncomeInput(
	val amount: BigDecimal,
	val incomeDate: LocalDate,
	val sourceCode: String,
	val note: String?,
)

@DgsComponent
class IncomeDataFetcher(
	private val incomeService: IncomeService,
) {
	@DgsQuery
	fun incomes(
		@InputArgument from: LocalDate,
		@InputArgument to: LocalDate,
	): List<IncomePayload> =
		incomeService.findByPeriod(from, to).map { income ->
			IncomePayload(
				id = income.id.toString(),
				amount = income.amount.amount,
				currency = income.amount.currency,
				incomeDate = income.incomeDate,
				source = SourcePayload(code = income.source.code, label = income.source.label),
				note = income.note,
			)
		}

	@DgsQuery
	fun sources(): List<SourcePayload> =
		incomeService.listSources().map { source ->
			SourcePayload(code = source.code, label = source.label)
		}

	@DgsMutation
	fun recordIncome(
		@InputArgument input: RecordIncomeInput,
	): IncomePayload {
		val income =
			incomeService.record(
				amount = input.amount,
				incomeDate = input.incomeDate,
				sourceCode = input.sourceCode,
				note = input.note,
			)
		return IncomePayload(
			id = income.id.toString(),
			amount = income.amount.amount,
			currency = income.amount.currency,
			incomeDate = income.incomeDate,
			source = SourcePayload(code = income.source.code, label = income.source.label),
			note = income.note,
		)
	}

	@DgsMutation
	fun editIncome(
		@InputArgument id: String,
		@InputArgument input: EditIncomeInput,
	): IncomePayload {
		val income =
			incomeService.edit(
				id = id.toLong(),
				amount = input.amount,
				incomeDate = input.incomeDate,
				sourceCode = input.sourceCode,
				note = input.note,
			)
		return IncomePayload(
			id = income.id.toString(),
			amount = income.amount.amount,
			currency = income.amount.currency,
			incomeDate = income.incomeDate,
			source = SourcePayload(code = income.source.code, label = income.source.label),
			note = income.note,
		)
	}

	@DgsMutation
	fun deleteIncome(
		@InputArgument id: String,
	): String {
		incomeService.delete(id.toLong())
		return id
	}
}
