import { useQuery } from "@apollo/client";
import { useState } from "react";

import { GET_INCOMES, type Income, type GetIncomesResult, type GetIncomesVariables } from "@/features/income/api/queries";
import { IncomeForm } from "@/features/income/components/IncomeForm";
import { IncomeList } from "@/features/income/components/IncomeList";
import { PeriodFilter } from "@/features/income/components/PeriodFilter";

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

export function IncomePage() {
  const [from, setFrom] = useState(today());
  const [to, setTo] = useState(today());
  const [editingIncome, setEditingIncome] = useState<Income | undefined>(undefined);

  const { data, loading, error } = useQuery<GetIncomesResult, GetIncomesVariables>(GET_INCOMES, {
    variables: { from, to },
  });

  return (
    <div>
      <h1>Income</h1>
      <IncomeForm
        key={editingIncome?.id ?? "new"}
        initialIncome={editingIncome}
        onSaved={() => setEditingIncome(undefined)}
      />
      <PeriodFilter
        from={from}
        to={to}
        onChange={(nextFrom, nextTo) => {
          setFrom(nextFrom);
          setTo(nextTo);
        }}
      />
      {loading && <p>Loading income…</p>}
      {error && <p>Could not load income: {error.message}</p>}
      {data && <IncomeList incomes={data.incomes} onEdit={setEditingIncome} />}
    </div>
  );
}
