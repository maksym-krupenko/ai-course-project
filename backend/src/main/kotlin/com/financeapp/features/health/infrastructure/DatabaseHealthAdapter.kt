package com.financeapp.features.health.infrastructure

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DatabaseHealthAdapter(
	private val jdbcTemplate: JdbcTemplate,
) {
	fun isReachable(): Boolean =
		runCatching { jdbcTemplate.queryForObject("SELECT 1", Int::class.java) == 1 }
			.getOrDefault(false)
}
