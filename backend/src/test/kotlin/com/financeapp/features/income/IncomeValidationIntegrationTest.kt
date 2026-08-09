package com.financeapp.features.income

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
class IncomeValidationIntegrationTest {
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
		mutation Record(${"$"}input: RecordIncomeInput!) {
			recordIncome(input: ${"$"}input) { id }
		}
		""".trimIndent()

	private val editMutation =
		"""
		mutation Edit(${"$"}id: ID!, ${"$"}input: EditIncomeInput!) {
			editIncome(id: ${"$"}id, input: ${"$"}input) { id }
		}
		""".trimIndent()

	private val deleteMutation =
		"""
		mutation Delete(${"$"}id: ID!) {
			deleteIncome(id: ${"$"}id)
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
							"incomeDate" to "2026-06-01",
							"sourceCode" to "SALARY",
							"note" to null,
						),
				),
			)

		assertTrue(result.errors.isNotEmpty())
	}

	@Test
	fun `recording an unknown source code comes back as a GraphQL error`() {
		val result =
			queryExecutor.execute(
				recordMutation,
				mapOf(
					"input" to
						mapOf(
							"amount" to "10.00",
							"incomeDate" to "2026-06-01",
							"sourceCode" to "NOT_A_SOURCE",
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
				query Incomes(${"$"}from: LocalDate!, ${"$"}to: LocalDate!) {
					incomes(from: ${"$"}from, to: ${"$"}to) { id }
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
							"incomeDate" to "2026-06-01",
							"sourceCode" to "SALARY",
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
