package com.financeapp.common.config

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsRuntimeWiring
import graphql.scalars.ExtendedScalars
import graphql.schema.idl.RuntimeWiring

@DgsComponent
class GraphQLScalarConfig {
	@DgsRuntimeWiring
	fun addScalars(builder: RuntimeWiring.Builder): RuntimeWiring.Builder =
		builder.scalar(ExtendedScalars.Date.transform { it.name("LocalDate") })
}
