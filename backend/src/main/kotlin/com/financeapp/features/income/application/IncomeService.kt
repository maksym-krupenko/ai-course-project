package com.financeapp.features.income.application

import com.financeapp.common.error.NotFoundException
import com.financeapp.features.income.domain.Income
import com.financeapp.features.income.domain.Money
import com.financeapp.features.income.domain.Period
import com.financeapp.features.income.domain.Source
import com.financeapp.features.income.infrastructure.IncomeRepository
import com.financeapp.features.income.infrastructure.toDomain
import com.financeapp.features.income.infrastructure.toEntity
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class IncomeService(
	private val incomeRepository: IncomeRepository,
) {
	fun record(
		amount: BigDecimal,
		incomeDate: LocalDate?,
		sourceCode: String,
		note: String?,
	): Income {
		val income =
			Income(
				amount = Money(amount = amount),
				incomeDate = incomeDate ?: LocalDate.now(),
				source = Source.fromCode(sourceCode),
				note = note,
			)
		return incomeRepository.save(income.toEntity()).toDomain()
	}

	fun edit(
		id: Long,
		amount: BigDecimal,
		incomeDate: LocalDate,
		sourceCode: String,
		note: String?,
	): Income {
		if (!incomeRepository.existsById(id)) {
			throw NotFoundException("No income found with id $id")
		}
		val income =
			Income(
				id = id,
				amount = Money(amount = amount),
				incomeDate = incomeDate,
				source = Source.fromCode(sourceCode),
				note = note,
			)
		return incomeRepository.save(income.toEntity()).toDomain()
	}

	fun delete(id: Long) {
		if (!incomeRepository.existsById(id)) {
			throw NotFoundException("No income found with id $id")
		}
		incomeRepository.deleteById(id)
	}

	fun findByPeriod(
		from: LocalDate,
		to: LocalDate,
	): List<Income> {
		val period = Period(from = from, to = to)
		return incomeRepository
			.findByIncomeDateBetweenOrderByIncomeDateDescIdDesc(period.from, period.to)
			.map { it.toDomain() }
	}

	fun listSources(): List<Source> = Source.all
}
