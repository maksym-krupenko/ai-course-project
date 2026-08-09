import { useQuery } from "@apollo/client";

import { GET_HEALTH, type HealthQueryResult } from "@/features/health/api/queries";
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/ui/card";

export function HealthStatus() {
  const { data, loading, error } = useQuery<HealthQueryResult>(GET_HEALTH);

  if (loading) return <p className="text-sm text-muted-foreground">Checking backend health…</p>;
  if (error) return <p className="text-sm text-destructive">Could not reach the backend: {error.message}</p>;

  return (
    <Card>
      <CardHeader>
        <CardTitle>Finance App</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-1 text-sm">
        <p>Backend status: {data?.health.status}</p>
        <p>Database reachable: {String(data?.health.databaseReachable)}</p>
        <p>Backend version: {data?.health.version}</p>
      </CardContent>
    </Card>
  );
}
