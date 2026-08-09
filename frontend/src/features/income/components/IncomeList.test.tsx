import { MockedProvider } from "@apollo/client/testing";
import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import type { Income } from "@/features/income/api/queries";
import { IncomeList } from "@/features/income/components/IncomeList";

describe("IncomeList", () => {
  it("renders income fields from the given incomes prop", () => {
    const incomes: Income[] = [
      {
        id: "1",
        amount: 3000,
        currency: "PLN",
        incomeDate: "2026-08-01",
        source: { code: "SALARY", label: "Salary" },
        note: "Monthly pay",
      },
    ];

    render(
      <MockedProvider>
        <IncomeList incomes={incomes} onEdit={vi.fn()} />
      </MockedProvider>,
    );

    expect(screen.getByText("2026-08-01")).toBeInTheDocument();
    expect(screen.getByText("Salary")).toBeInTheDocument();
    expect(screen.getByText("3000 PLN")).toBeInTheDocument();
    expect(screen.getByText("Monthly pay")).toBeInTheDocument();
  });
});
