package com.financeapp.features.health.domain

data class HealthStatus(
	val status: String,
	val databaseReachable: Boolean,
	val version: String,
)

/**
 * Port owned by the domain layer. Infrastructure adapters implement this;
 * the domain and application layers never depend on a concrete data-access technology.
 */
interface DatabaseHealthPort {
	fun isReachable(): Boolean
}
