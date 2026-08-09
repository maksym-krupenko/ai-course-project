package com.financeapp.features.health

import com.financeapp.features.health.application.HealthCheckService
import com.financeapp.features.health.infrastructure.DatabaseHealthAdapter
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthCheckServiceTest {
	@Test
	fun `reports UP when database is reachable`() {
		val adapter = mockk<DatabaseHealthAdapter> { every { isReachable() } returns true }
		val service = HealthCheckService(adapter, appVersion = "test")

		val result = service.check()

		assertEquals("UP", result.status)
		assertTrue(result.databaseReachable)
	}

	@Test
	fun `reports DEGRADED when database is unreachable`() {
		val adapter = mockk<DatabaseHealthAdapter> { every { isReachable() } returns false }
		val service = HealthCheckService(adapter, appVersion = "test")

		val result = service.check()

		assertEquals("DEGRADED", result.status)
		assertTrue(!result.databaseReachable)
	}
}
