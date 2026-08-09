package com.financeapp.features.health.application

import com.financeapp.features.health.domain.HealthStatus
import com.financeapp.features.health.infrastructure.DatabaseHealthAdapter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class HealthCheckService(
	private val databaseHealthAdapter: DatabaseHealthAdapter,
	@Value("\${app.version}") private val appVersion: String,
) {
	fun check(): HealthStatus {
		val dbReachable = databaseHealthAdapter.isReachable()
		return HealthStatus(
			status = if (dbReachable) "UP" else "DEGRADED",
			databaseReachable = dbReachable,
			version = appVersion,
		)
	}
}
