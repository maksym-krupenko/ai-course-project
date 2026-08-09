import { MockedProvider } from "@apollo/client/testing";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { GET_SOURCES, RECORD_INCOME } from "@/features/income/api/queries";
import { IncomeForm } from "@/features/income/components/IncomeForm";

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

describe("IncomeForm", () => {
  it("submits a new income with only amount and source filled in", async () => {
    let recordIncomeCalled = false;

    const mocks = [
      {
        request: { query: GET_SOURCES },
        result: {
          data: {
            sources: [
              { __typename: "Source", code: "SALARY", label: "Salary" },
              { __typename: "Source", code: "GIFT", label: "Gift" },
            ],
          },
        },
      },
      {
        request: {
          query: RECORD_INCOME,
          variables: {
            input: {
              amount: 3000,
              incomeDate: today(),
              sourceCode: "SALARY",
              note: null,
            },
          },
        },
        result: () => {
          recordIncomeCalled = true;
          return {
            data: {
              recordIncome: {
                __typename: "Income",
                id: "1",
                amount: 3000,
                currency: "PLN",
                incomeDate: today(),
                source: { __typename: "Source", code: "SALARY", label: "Salary" },
                note: null,
              },
            },
          };
        },
      },
    ];

    render(
      <MockedProvider mocks={mocks}>
        <IncomeForm />
      </MockedProvider>,
    );

    await screen.findByText("Salary");

    fireEvent.change(screen.getByLabelText("Amount"), { target: { value: "3000" } });
    fireEvent.change(screen.getByLabelText("Source"), { target: { value: "SALARY" } });
    fireEvent.click(screen.getByRole("button", { name: "Record income" }));

    await screen.findByRole("button", { name: "Record income" });
    expect(recordIncomeCalled).toBe(true);
  });
});
