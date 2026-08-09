import { useQuery } from "@apollo/client";
import { useState } from "react";

import { GET_INCOMES, type Income, type GetIncomesResult, type GetIncomesVariables } from "@/features/income/api/queries";
import { IncomeForm } from "@/features/income/components/IncomeForm";
import { IncomeList } from "@/features/income/components/IncomeList";
import { PeriodFilter } from "@/features/income/components/PeriodFilter";
import { Card, CardContent } from "@/shared/components/ui/card";

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
    <div className="flex flex-col gap-6">
      <h1 className="font-heading text-2xl font-semibold">Income</h1>
      <Card>
        <CardContent>
          <IncomeForm
            key={editingIncome?.id ?? "new"}
            initialIncome={editingIncome}
            onSaved={() => setEditingIncome(undefined)}
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
      {loading && <p className="text-sm text-muted-foreground">Loading income…</p>}
      {error && <p className="text-sm text-destructive">Could not load income: {error.message}</p>}
      {data && (
        <Card>
          <IncomeList incomes={data.incomes} onEdit={setEditingIncome} />
        </Card>
      )}
    </div>
  );
}
