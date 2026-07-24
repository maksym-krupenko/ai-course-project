import { MockedProvider } from "@apollo/client/testing";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { GET_HEALTH } from "@/features/health/api/queries";
import { HealthStatus } from "@/features/health/components/HealthStatus";

describe("HealthStatus", () => {
  it("renders the backend health once the query resolves", async () => {
    const mocks = [
      {
        request: { query: GET_HEALTH },
        result: {
          data: {
            health: {
              __typename: "HealthPayload",
              status: "UP",
              databaseReachable: true,
              version: "0.0.1-SNAPSHOT",
            },
          },
        },
      },
    ];

    render(
      <MockedProvider mocks={mocks}>
        <HealthStatus />
      </MockedProvider>,
    );

    expect(await screen.findByText(/Backend status: UP/)).toBeInTheDocument();
  });
});
