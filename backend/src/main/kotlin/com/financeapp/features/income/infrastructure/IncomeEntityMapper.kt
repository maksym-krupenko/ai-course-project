package com.financeapp.features.income.infrastructure

import com.financeapp.features.income.domain.Income
import com.financeapp.features.income.domain.Money
import com.financeapp.features.income.domain.Source

fun Income.toEntity(): IncomeEntity =
	IncomeEntity(
		id = id,
		amount = amount.amount,
		currency = amount.currency,
		incomeDate = incomeDate,
		source = source.code,
		note = note,
	)

fun IncomeEntity.toDomain(): Income =
	Income(
		id = id,
		amount = Money(amount = amount, currency = currency),
		incomeDate = incomeDate,
		source = Source.fromCode(source),
		note = note,
	)
