package com.financeapp.features.health.application

import com.financeapp.features.health.domain.DatabaseHealthPort
import com.financeapp.features.health.domain.HealthStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class HealthCheckService(
	private val databaseHealthPort: DatabaseHealthPort,
	@Value("\${app.version}") private val appVersion: String,
) {
	fun check(): HealthStatus {
		val dbReachable = databaseHealthPort.isReachable()
		return HealthStatus(
			status = if (dbReachable) "UP" else "DEGRADED",
			databaseReachable = dbReachable,
			version = appVersion,
		)
	}
}
