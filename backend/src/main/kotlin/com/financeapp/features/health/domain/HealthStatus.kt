package com.financeapp.features.health.domain

data class HealthStatus(
	val status: String,
	val databaseReachable: Boolean,
	val version: String,
)
