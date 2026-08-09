package com.financeapp.features.income.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface IncomeRepository : JpaRepository<IncomeEntity, Long> {
	fun findByIncomeDateBetweenOrderByIncomeDateDescIdDesc(
		from: LocalDate,
		to: LocalDate,
	): List<IncomeEntity>
}
