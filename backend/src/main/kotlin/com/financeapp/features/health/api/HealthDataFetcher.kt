package com.financeapp.features.health.api

import com.financeapp.features.health.application.HealthCheckService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery

data class HealthPayload(
	val status: String,
	val databaseReachable: Boolean,
	val version: String,
)

@DgsComponent
class HealthDataFetcher(
	private val healthCheckService: HealthCheckService,
) {
	@DgsQuery
	fun health(): HealthPayload {
		val result = healthCheckService.check()
		return HealthPayload(
			status = result.status,
			databaseReachable = result.databaseReachable,
			version = result.version,
		)
	}
}
