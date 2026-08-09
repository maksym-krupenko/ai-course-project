package com.financeapp.features.expense

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
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Testcontainers
@SpringBootTest
class ExpenseMutationIntegrationTest {
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

	private val recordMutation =
		"""
		mutation Record(${"$"}input: RecordExpenseInput!) {
			recordExpense(input: ${"$"}input) {
				id
				amount
				expenseDate
				note
				category { code label }
			}
		}
		""".trimIndent()

	private val editMutation =
		"""
		mutation Edit(${"$"}id: ID!, ${"$"}input: EditExpenseInput!) {
			editExpense(id: ${"$"}id, input: ${"$"}input) {
				id
				amount
				expenseDate
				note
				category { code label }
			}
		}
		""".trimIndent()

	private val deleteMutation =
		"""
		mutation Delete(${"$"}id: ID!) {
			deleteExpense(id: ${"$"}id)
		}
		""".trimIndent()

	@Test
	fun `record, edit, and delete an expense round trip through GraphQL`() {
		expenseRepository.deleteAll()

		val recordedId =
			queryExecutor.executeAndExtractJsonPath<String>(
				recordMutation,
				"data.recordExpense.id",
				mapOf(
					"input" to
						mapOf(
							"amount" to "30.00",
							"expenseDate" to "2026-06-01",
							"categoryCode" to "GROCERIES",
							"note" to "Weekly shop",
						),
				),
			)
		val recordedCategoryCode =
			queryExecutor.executeAndExtractJsonPath<String>(
				recordMutation,
				"data.recordExpense.category.code",
				mapOf(
					"input" to
						mapOf(
							"amount" to "30.00",
							"expenseDate" to "2026-06-01",
							"categoryCode" to "GROCERIES",
							"note" to "Weekly shop",
						),
				),
			)
		assertEquals("GROCERIES", recordedCategoryCode)

		val editedNote =
			queryExecutor.executeAndExtractJsonPath<String>(
				editMutation,
				"data.editExpense.note",
				mapOf(
					"id" to recordedId,
					"input" to
						mapOf(
							"amount" to "45.00",
							"expenseDate" to "2026-06-02",
							"categoryCode" to "TRANSPORT",
							"note" to "Updated",
						),
				),
			)
		assertEquals("Updated", editedNote)

		val deletedId =
			queryExecutor.executeAndExtractJsonPath<String>(
				deleteMutation,
				"data.deleteExpense",
				mapOf("id" to recordedId),
			)
		assertEquals(recordedId, deletedId)
		assertNull(expenseRepository.findById(recordedId.toLong()).orElse(null))
	}

	@Test
	fun `recording an expense with a future date succeeds`() {
		expenseRepository.deleteAll()
		val futureDate = LocalDate.now().plusYears(1).toString()

		val expenseDate =
			queryExecutor.executeAndExtractJsonPath<String>(
				recordMutation,
				"data.recordExpense.expenseDate",
				mapOf(
					"input" to
						mapOf(
							"amount" to "10.00",
							"expenseDate" to futureDate,
							"categoryCode" to "OTHER",
							"note" to null,
						),
				),
			)

		assertEquals(futureDate, expenseDate)
	}
}
