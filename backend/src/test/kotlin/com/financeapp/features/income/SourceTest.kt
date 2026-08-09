package com.financeapp.features.income

import com.financeapp.common.error.DomainValidationException
import com.financeapp.features.income.domain.Source
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SourceTest {
	@Test
	fun `fromCode resolves each known code to its object`() {
		Source.all.forEach { source ->
			assertEquals(source, Source.fromCode(source.code))
		}
	}

	@Test
	fun `fromCode throws for an unknown code`() {
		assertFailsWith<DomainValidationException> {
			Source.fromCode("NOT_A_SOURCE")
		}
	}
}
