package com.financeapp.features.expense

import com.financeapp.features.expense.domain.Category
import com.financeapp.features.expense.infrastructure.ExpenseEntity
import com.financeapp.features.expense.infrastructure.ExpenseRepository
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
class ExpenseQueryIntegrationTest {
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
	private lateinit var expenseRepository: ExpenseRepository

	@Test
	fun `expenses query retrieves records for an arbitrary non-calendar-month period`() {
		expenseRepository.deleteAll()
		expenseRepository.save(
			ExpenseEntity(
				amount = BigDecimal("25.00"),
				currency = "PLN",
				expenseDate = LocalDate.of(2026, 1, 20),
				category = "GROCERIES",
				note = null,
			),
		)
		expenseRepository.save(
			ExpenseEntity(
				amount = BigDecimal("40.00"),
				currency = "PLN",
				expenseDate = LocalDate.of(2026, 2, 5),
				category = "TRANSPORT",
				note = null,
			),
		)
		expenseRepository.save(
			ExpenseEntity(
				amount = BigDecimal("99.00"),
				currency = "PLN",
				expenseDate = LocalDate.of(2026, 3, 1),
				category = "OTHER",
				note = null,
			),
		)

		val amounts =
			queryExecutor.executeAndExtractJsonPath<List<Any>>(
				"""
				query Expenses(${"$"}from: LocalDate!, ${"$"}to: LocalDate!) {
					expenses(from: ${"$"}from, to: ${"$"}to) { amount }
				}
				""".trimIndent(),
				"data.expenses[*].amount",
				mapOf("from" to "2026-01-15", "to" to "2026-01-31"),
			)

		assertEquals(1, amounts.size)
	}

	@Test
	fun `expenses query supports a single-day lookup where from equals to`() {
		expenseRepository.deleteAll()
		expenseRepository.save(
			ExpenseEntity(
				amount = BigDecimal("15.00"),
				currency = "PLN",
				expenseDate = LocalDate.of(2026, 5, 10),
				category = "DINING_OUT",
				note = null,
			),
		)
		expenseRepository.save(
			ExpenseEntity(
				amount = BigDecimal("15.00"),
				currency = "PLN",
				expenseDate = LocalDate.of(2026, 5, 11),
				category = "DINING_OUT",
				note = null,
			),
		)

		val ids =
			queryExecutor.executeAndExtractJsonPath<List<Any>>(
				"""
				query Expenses(${"$"}from: LocalDate!, ${"$"}to: LocalDate!) {
					expenses(from: ${"$"}from, to: ${"$"}to) { id }
				}
				""".trimIndent(),
				"data.expenses[*].id",
				mapOf("from" to "2026-05-10", "to" to "2026-05-10"),
			)

		assertEquals(1, ids.size)
	}

	@Test
	fun `categories query returns code and label pairs for the full fixed set`() {
		val codes =
			queryExecutor.executeAndExtractJsonPath<List<String>>(
				"{ categories { code label } }",
				"data.categories[*].code",
			)

		assertEquals(Category.all.size, codes.size)
		assertTrue(Category.all.map { it.code }.all { it in codes })
	}
}
