import { useQuery } from "@apollo/client";
import { useState } from "react";

import { GET_EXPENSES, type Expense, type GetExpensesResult, type GetExpensesVariables } from "@/features/expense/api/queries";
import { ExpenseForm } from "@/features/expense/components/ExpenseForm";
import { ExpenseList } from "@/features/expense/components/ExpenseList";
import { PeriodFilter } from "@/features/expense/components/PeriodFilter";

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

export function ExpensePage() {
  const [from, setFrom] = useState(today());
  const [to, setTo] = useState(today());
  const [editingExpense, setEditingExpense] = useState<Expense | undefined>(undefined);

  const { data, loading, error } = useQuery<GetExpensesResult, GetExpensesVariables>(GET_EXPENSES, {
    variables: { from, to },
  });

  return (
    <div>
      <h1>Expenses</h1>
      <ExpenseForm
        key={editingExpense?.id ?? "new"}
        initialExpense={editingExpense}
        onSaved={() => setEditingExpense(undefined)}
      />
      <PeriodFilter
        from={from}
        to={to}
        onChange={(nextFrom, nextTo) => {
          setFrom(nextFrom);
          setTo(nextTo);
        }}
      />
      {loading && <p>Loading expenses…</p>}
      {error && <p>Could not load expenses: {error.message}</p>}
      {data && <ExpenseList expenses={data.expenses} onEdit={setEditingExpense} />}
    </div>
  );
}
