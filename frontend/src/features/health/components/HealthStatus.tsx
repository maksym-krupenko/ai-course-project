import { useQuery } from "@apollo/client";

import { GET_HEALTH, type HealthQueryResult } from "@/features/health/api/queries";

export function HealthStatus() {
  const { data, loading, error } = useQuery<HealthQueryResult>(GET_HEALTH);

  if (loading) return <p>Checking backend health…</p>;
  if (error) return <p>Could not reach the backend: {error.message}</p>;

  return (
    <div>
      <h1>Finance App</h1>
      <p>Backend status: {data?.health.status}</p>
      <p>Database reachable: {String(data?.health.databaseReachable)}</p>
      <p>Backend version: {data?.health.version}</p>
    </div>
  );
}
