package com.financeapp.features.expense

import com.financeapp.common.error.DomainValidationException
import com.financeapp.features.expense.domain.Category
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CategoryTest {
	@Test
	fun `fromCode resolves each known code to its object`() {
		Category.all.forEach { category ->
			assertEquals(category, Category.fromCode(category.code))
		}
	}

	@Test
	fun `fromCode throws for an unknown code`() {
		assertFailsWith<DomainValidationException> {
			Category.fromCode("NOT_A_CATEGORY")
		}
	}
}
