import { MockedProvider } from "@apollo/client/testing";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { GET_CATEGORIES, RECORD_EXPENSE } from "@/features/expense/api/queries";
import { ExpenseForm } from "@/features/expense/components/ExpenseForm";

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

describe("ExpenseForm", () => {
  it("submits a new expense with only amount and category filled in", async () => {
    let recordExpenseCalled = false;

    const mocks = [
      {
        request: { query: GET_CATEGORIES },
        result: {
          data: {
            categories: [
              { __typename: "Category", code: "GROCERIES", label: "Groceries" },
              { __typename: "Category", code: "TRANSPORT", label: "Transport" },
            ],
          },
        },
      },
      {
        request: {
          query: RECORD_EXPENSE,
          variables: {
            input: {
              amount: 12.5,
              expenseDate: today(),
              categoryCode: "GROCERIES",
              note: null,
            },
          },
        },
        result: () => {
          recordExpenseCalled = true;
          return {
            data: {
              recordExpense: {
                __typename: "Expense",
                id: "1",
                amount: 12.5,
                currency: "PLN",
                expenseDate: today(),
                category: { __typename: "Category", code: "GROCERIES", label: "Groceries" },
                note: null,
              },
            },
          };
        },
      },
    ];

    render(
      <MockedProvider mocks={mocks}>
        <ExpenseForm />
      </MockedProvider>,
    );

    await screen.findByText("Groceries");

    fireEvent.change(screen.getByLabelText("Amount"), { target: { value: "12.5" } });
    fireEvent.change(screen.getByLabelText("Category"), { target: { value: "GROCERIES" } });
    fireEvent.click(screen.getByRole("button", { name: "Record expense" }));

    await screen.findByRole("button", { name: "Record expense" });
    expect(recordExpenseCalled).toBe(true);
  });
});
