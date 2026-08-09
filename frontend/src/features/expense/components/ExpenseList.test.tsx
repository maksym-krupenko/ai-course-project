import { MockedProvider } from "@apollo/client/testing";
import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import type { Expense } from "@/features/expense/api/queries";
import { ExpenseList } from "@/features/expense/components/ExpenseList";

describe("ExpenseList", () => {
  it("renders expense fields from the given expenses prop", () => {
    const expenses: Expense[] = [
      {
        id: "1",
        amount: 42.5,
        currency: "PLN",
        expenseDate: "2026-08-01",
        category: { code: "GROCERIES", label: "Groceries" },
        note: "Weekly shop",
      },
    ];

    render(
      <MockedProvider>
        <ExpenseList expenses={expenses} onEdit={vi.fn()} />
      </MockedProvider>,
    );

    expect(screen.getByText("2026-08-01")).toBeInTheDocument();
    expect(screen.getByText("Groceries")).toBeInTheDocument();
    expect(screen.getByText("42.5 PLN")).toBeInTheDocument();
    expect(screen.getByText("Weekly shop")).toBeInTheDocument();
  });
});
