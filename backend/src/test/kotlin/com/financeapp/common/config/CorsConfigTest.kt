package com.financeapp.common.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {
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
	private lateinit var mockMvc: MockMvc

	@Test
	fun `allows a cross-origin preflight from the configured frontend origin`() {
		mockMvc
			.perform(
				options("/graphql")
					.header("Origin", "http://localhost:5173")
					.header("Access-Control-Request-Method", "POST"),
			)
			.andExpect(status().isOk)
			.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
	}

	@Test
	fun `rejects a cross-origin preflight from an unconfigured origin`() {
		mockMvc
			.perform(
				options("/graphql")
					.header("Origin", "http://evil.example.com")
					.header("Access-Control-Request-Method", "POST"),
			)
			.andExpect(status().isForbidden)
	}
}
