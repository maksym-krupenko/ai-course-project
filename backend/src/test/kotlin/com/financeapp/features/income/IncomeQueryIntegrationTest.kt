package com.financeapp.features.income

import com.financeapp.features.income.domain.Source
import com.financeapp.features.income.infrastructure.IncomeEntity
import com.financeapp.features.income.infrastructure.IncomeRepository
import com.netflix.graphql.dgs.DgsQueryExecutor
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Testcontainers
@SpringBootTest
class IncomeQueryIntegrationTest {
	companion object {
		@Container
		@JvmStatic
		val postgres = PostgreSQLContainer("postgres:16-alpine")

		@JvmStatic
		@DynamicPropertySource
		fun registerDatasource(registry: DynamicPropertyRegistry) {
			registry.add("spring.datasource.url", postgres::getJdbcUrl)
			registry.add("spring.datasource.username", postgres::getUsername)
			registry.add("spring.datasource.password", postgres::getPassword)
		}
	}

	@Autowired
	private lateinit var queryExecutor: DgsQueryExecutor

	@Autowired
	private lateinit var incomeRepository: IncomeRepository

	@Test
	fun `incomes query retrieves records for an arbitrary non-calendar-month period`() {
		incomeRepository.deleteAll()
		incomeRepository.save(
			IncomeEntity(
				amount = BigDecimal("2500.00"),
				currency = "PLN",
				incomeDate = LocalDate.of(2026, 1, 20),
				source = "SALARY",
				note = null,
			),
		)
		incomeRepository.save(
			IncomeEntity(
				amount = BigDecimal("400.00"),
				currency = "PLN",
				incomeDate = LocalDate.of(2026, 2, 5),
				source = "FREELANCE_SIDE_INCOME",
				note = null,
			),
		)
		incomeRepository.save(
			IncomeEntity(
				amount = BigDecimal("99.00"),
				currency = "PLN",
				incomeDate = LocalDate.of(2026, 3, 1),
				source = "OTHER",
				note = null,
			),
		)

		val amounts =
			queryExecutor.executeAndExtractJsonPath<List<Any>>(
				"""
				query Incomes(${"$"}from: LocalDate!, ${"$"}to: LocalDate!) {
					incomes(from: ${"$"}from, to: ${"$"}to) { amount }
				}
				""".trimIndent(),
				"data.incomes[*].amount",
				mapOf("from" to "2026-01-15", "to" to "2026-01-31"),
			)

		assertEquals(1, amounts.size)
	}

	@Test
	fun `incomes query supports a single-day lookup where from equals to`() {
		incomeRepository.deleteAll()
		incomeRepository.save(
			IncomeEntity(
				amount = BigDecimal("15.00"),
				currency = "PLN",
				incomeDate = LocalDate.of(2026, 5, 10),
				source = "GIFT",
				note = null,
			),
		)
		incomeRepository.save(
			IncomeEntity(
				amount = BigDecimal("15.00"),
				currency = "PLN",
				incomeDate = LocalDate.of(2026, 5, 11),
				source = "GIFT",
				note = null,
			),
		)

		val ids =
			queryExecutor.executeAndExtractJsonPath<List<Any>>(
				"""
				query Incomes(${"$"}from: LocalDate!, ${"$"}to: LocalDate!) {
					incomes(from: ${"$"}from, to: ${"$"}to) { id }
				}
				""".trimIndent(),
				"data.incomes[*].id",
				mapOf("from" to "2026-05-10", "to" to "2026-05-10"),
			)

		assertEquals(1, ids.size)
	}

	@Test
	fun `sources query returns code and label pairs for the full fixed set`() {
		val codes =
			queryExecutor.executeAndExtractJsonPath<List<String>>(
				"{ sources { code label } }",
				"data.sources[*].code",
			)

		assertEquals(Source.all.size, codes.size)
		assertTrue(Source.all.map { it.code }.all { it in codes })
	}
}
