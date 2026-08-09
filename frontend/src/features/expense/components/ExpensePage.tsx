import { useQuery } from "@apollo/client";
import { useState } from "react";

import { GET_EXPENSES, type Expense, type GetExpensesResult, type GetExpensesVariables } from "@/features/expense/api/queries";
import { ExpenseForm } from "@/features/expense/components/ExpenseForm";
import { ExpenseList } from "@/features/expense/components/ExpenseList";
import { PeriodFilter } from "@/features/expense/components/PeriodFilter";
import { Card, CardContent } from "@/shared/components/ui/card";

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
    <div className="flex flex-col gap-6">
      <h1 className="font-heading text-2xl font-semibold">Expenses</h1>
      <Card>
        <CardContent>
          <ExpenseForm
            key={editingExpense?.id ?? "new"}
            initialExpense={editingExpense}
            onSaved={() => setEditingExpense(undefined)}
          />
        </CardContent>
      </Card>
      <PeriodFilter
        from={from}
        to={to}
        onChange={(nextFrom, nextTo) => {
          setFrom(nextFrom);
          setTo(nextTo);
        }}
      />
      {loading && <p className="text-sm text-muted-foreground">Loading expenses…</p>}
      {error && <p className="text-sm text-destructive">Could not load expenses: {error.message}</p>}
      {data && (
        <Card>
          <ExpenseList expenses={data.expenses} onEdit={setEditingExpense} />
        </Card>
      )}
    </div>
  );
}
