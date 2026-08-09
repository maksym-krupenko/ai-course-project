package com.financeapp.features.expense

import com.netflix.graphql.dgs.DgsQueryExecutor
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertTrue

@Testcontainers
@SpringBootTest
class ExpenseValidationIntegrationTest {
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

	private val recordMutation =
		"""
		mutation Record(${"$"}input: RecordExpenseInput!) {
			recordExpense(input: ${"$"}input) { id }
		}
		""".trimIndent()

	private val editMutation =
		"""
		mutation Edit(${"$"}id: ID!, ${"$"}input: EditExpenseInput!) {
			editExpense(id: ${"$"}id, input: ${"$"}input) { id }
		}
		""".trimIndent()

	private val deleteMutation =
		"""
		mutation Delete(${"$"}id: ID!) {
			deleteExpense(id: ${"$"}id)
		}
		""".trimIndent()

	@Test
	fun `recording a zero amount comes back as a GraphQL error, not a crash`() {
		val result =
			queryExecutor.execute(
				recordMutation,
				mapOf(
					"input" to
						mapOf(
							"amount" to "0",
							"expenseDate" to "2026-06-01",
							"categoryCode" to "GROCERIES",
							"note" to null,
						),
				),
			)

		assertTrue(result.errors.isNotEmpty())
	}

	@Test
	fun `recording an unknown category code comes back as a GraphQL error`() {
		val result =
			queryExecutor.execute(
				recordMutation,
				mapOf(
					"input" to
						mapOf(
							"amount" to "10.00",
							"expenseDate" to "2026-06-01",
							"categoryCode" to "NOT_A_CATEGORY",
							"note" to null,
						),
				),
			)

		assertTrue(result.errors.isNotEmpty())
	}

	@Test
	fun `querying with from after to comes back as a GraphQL error`() {
		val result =
			queryExecutor.execute(
				"""
				query Expenses(${"$"}from: LocalDate!, ${"$"}to: LocalDate!) {
					expenses(from: ${"$"}from, to: ${"$"}to) { id }
				}
				""".trimIndent(),
				mapOf("from" to "2026-06-10", "to" to "2026-06-01"),
			)

		assertTrue(result.errors.isNotEmpty())
	}

	@Test
	fun `editing a missing id comes back as a GraphQL error`() {
		val result =
			queryExecutor.execute(
				editMutation,
				mapOf(
					"id" to "999999",
					"input" to
						mapOf(
							"amount" to "10.00",
							"expenseDate" to "2026-06-01",
							"categoryCode" to "GROCERIES",
							"note" to null,
						),
				),
			)

		assertTrue(result.errors.isNotEmpty())
	}

	@Test
	fun `deleting a missing id comes back as a GraphQL error`() {
		val result = queryExecutor.execute(deleteMutation, mapOf("id" to "999999"))

		assertTrue(result.errors.isNotEmpty())
	}
}
