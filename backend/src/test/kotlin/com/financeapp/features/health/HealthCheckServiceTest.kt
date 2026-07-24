package com.financeapp.features.health

import com.financeapp.features.health.application.HealthCheckService
import com.financeapp.features.health.domain.DatabaseHealthPort
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthCheckServiceTest {
	@Test
	fun `reports UP when database is reachable`() {
		val port = mockk<DatabaseHealthPort> { every { isReachable() } returns true }
		val service = HealthCheckService(port, appVersion = "test")

		val result = service.check()

		assertEquals("UP", result.status)
		assertTrue(result.databaseReachable)
	}

	@Test
	fun `reports DEGRADED when database is unreachable`() {
		val port = mockk<DatabaseHealthPort> { every { isReachable() } returns false }
		val service = HealthCheckService(port, appVersion = "test")

		val result = service.check()

		assertEquals("DEGRADED", result.status)
		assertTrue(!result.databaseReachable)
	}
}
