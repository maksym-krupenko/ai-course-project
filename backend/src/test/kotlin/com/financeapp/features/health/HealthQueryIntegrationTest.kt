package com.financeapp.features.health

import com.netflix.graphql.dgs.DgsQueryExecutor
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals

@Testcontainers
@SpringBootTest
class HealthQueryIntegrationTest {
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

	@Test
	fun `health query reports the database as reachable after Flyway migrates it`() {
		val status =
			queryExecutor.executeAndExtractJsonPath<String>(
				"{ health { status databaseReachable } }",
				"data.health.status",
			)

		assertEquals("UP", status)
	}
}
