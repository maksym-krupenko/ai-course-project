import { MockedProvider } from "@apollo/client/testing";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { GET_INCOMES, GET_SOURCES } from "@/features/income/api/queries";
import { IncomePage } from "@/features/income/components/IncomePage";

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

describe("IncomePage", () => {
  it("renders the income list from the mocked query data", async () => {
    const mocks = [
      {
        request: { query: GET_SOURCES },
        result: { data: { sources: [{ __typename: "Source", code: "SALARY", label: "Salary" }] } },
      },
      {
        request: { query: GET_INCOMES, variables: { from: today(), to: today() } },
        result: {
          data: {
            incomes: [
              {
                __typename: "Income",
                id: "1",
                amount: 3000,
                currency: "PLN",
                incomeDate: today(),
                source: { __typename: "Source", code: "SALARY", label: "Salary" },
                note: null,
              },
            ],
          },
        },
      },
    ];

    render(
      <MockedProvider mocks={mocks}>
        <IncomePage />
      </MockedProvider>,
    );

    expect(await screen.findByText("Salary", { selector: "td" })).toBeInTheDocument();
  });
});
