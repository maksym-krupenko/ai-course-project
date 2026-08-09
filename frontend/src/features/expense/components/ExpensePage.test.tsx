import { MockedProvider } from "@apollo/client/testing";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { GET_CATEGORIES, GET_EXPENSES } from "@/features/expense/api/queries";
import { ExpensePage } from "@/features/expense/components/ExpensePage";

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

describe("ExpensePage", () => {
  it("renders the expense list from the mocked query data", async () => {
    const mocks = [
      {
        request: { query: GET_CATEGORIES },
        result: { data: { categories: [{ __typename: "Category", code: "GROCERIES", label: "Groceries" }] } },
      },
      {
        request: { query: GET_EXPENSES, variables: { from: today(), to: today() } },
        result: {
          data: {
            expenses: [
              {
                __typename: "Expense",
                id: "1",
                amount: 20,
                currency: "PLN",
                expenseDate: today(),
                category: { __typename: "Category", code: "GROCERIES", label: "Groceries" },
                note: null,
              },
            ],
          },
        },
      },
    ];

    render(
      <MockedProvider mocks={mocks}>
        <ExpensePage />
      </MockedProvider>,
    );

    expect(await screen.findByText("Groceries", { selector: "td" })).toBeInTheDocument();
  });
});
