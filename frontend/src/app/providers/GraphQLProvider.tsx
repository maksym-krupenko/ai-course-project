import { ApolloClient, ApolloProvider, HttpLink, InMemoryCache } from "@apollo/client";
import type { PropsWithChildren } from "react";

const client = new ApolloClient({
  link: new HttpLink({
    uri: import.meta.env.VITE_GRAPHQL_URL ?? "http://localhost:8080/graphql",
  }),
  cache: new InMemoryCache(),
});

export function GraphQLProvider({ children }: PropsWithChildren) {
  return <ApolloProvider client={client}>{children}</ApolloProvider>;
}
