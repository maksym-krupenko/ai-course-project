package com.financeapp.features.income

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
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Testcontainers
@SpringBootTest
class IncomeMutationIntegrationTest {
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

	private val recordMutation =
		"""
		mutation Record(${"$"}input: RecordIncomeInput!) {
			recordIncome(input: ${"$"}input) {
				id
				amount
				incomeDate
				note
				source { code label }
			}
		}
		""".trimIndent()

	private val editMutation =
		"""
		mutation Edit(${"$"}id: ID!, ${"$"}input: EditIncomeInput!) {
			editIncome(id: ${"$"}id, input: ${"$"}input) {
				id
				amount
				incomeDate
				note
				source { code label }
			}
		}
		""".trimIndent()

	private val deleteMutation =
		"""
		mutation Delete(${"$"}id: ID!) {
			deleteIncome(id: ${"$"}id)
		}
		""".trimIndent()

	@Test
	fun `record, edit, and delete an income round trip through GraphQL`() {
		incomeRepository.deleteAll()

		val recordedId =
			queryExecutor.executeAndExtractJsonPath<String>(
				recordMutation,
				"data.recordIncome.id",
				mapOf(
					"input" to
						mapOf(
							"amount" to "3000.00",
							"incomeDate" to "2026-06-01",
							"sourceCode" to "SALARY",
							"note" to "Monthly pay",
						),
				),
			)
		val recordedSourceCode =
			queryExecutor.executeAndExtractJsonPath<String>(
				recordMutation,
				"data.recordIncome.source.code",
				mapOf(
					"input" to
						mapOf(
							"amount" to "3000.00",
							"incomeDate" to "2026-06-01",
							"sourceCode" to "SALARY",
							"note" to "Monthly pay",
						),
				),
			)
		assertEquals("SALARY", recordedSourceCode)

		val editedNote =
			queryExecutor.executeAndExtractJsonPath<String>(
				editMutation,
				"data.editIncome.note",
				mapOf(
					"id" to recordedId,
					"input" to
						mapOf(
							"amount" to "3200.00",
							"incomeDate" to "2026-06-02",
							"sourceCode" to "FREELANCE_SIDE_INCOME",
							"note" to "Updated",
						),
				),
			)
		assertEquals("Updated", editedNote)

		val deletedId =
			queryExecutor.executeAndExtractJsonPath<String>(
				deleteMutation,
				"data.deleteIncome",
				mapOf("id" to recordedId),
			)
		assertEquals(recordedId, deletedId)
		assertNull(incomeRepository.findById(recordedId.toLong()).orElse(null))
	}

	@Test
	fun `recording an income with a future date succeeds`() {
		incomeRepository.deleteAll()
		val futureDate = LocalDate.now().plusYears(1).toString()

		val incomeDate =
			queryExecutor.executeAndExtractJsonPath<String>(
				recordMutation,
				"data.recordIncome.incomeDate",
				mapOf(
					"input" to
						mapOf(
							"amount" to "10.00",
							"incomeDate" to futureDate,
							"sourceCode" to "OTHER",
							"note" to null,
						),
				),
			)

		assertEquals(futureDate, incomeDate)
	}
}
