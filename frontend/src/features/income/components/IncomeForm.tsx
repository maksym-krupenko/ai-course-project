import { useMutation, useQuery } from "@apollo/client";
import { type FormEvent, useState } from "react";

import {
  EDIT_INCOME,
  type EditIncomeResult,
  type EditIncomeVariables,
  GET_SOURCES,
  type GetSourcesResult,
  type Income,
  RECORD_INCOME,
  type RecordIncomeResult,
  type RecordIncomeVariables,
} from "@/features/income/api/queries";
import { Button } from "@/shared/components/ui/button";
import { Input } from "@/shared/components/ui/input";
import { Label } from "@/shared/components/ui/label";
import { Textarea } from "@/shared/components/ui/textarea";

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

interface IncomeFormProps {
  initialIncome?: Income;
  onSaved?: () => void;
}

export function IncomeForm({ initialIncome, onSaved }: IncomeFormProps) {
  const isEditing = initialIncome !== undefined;
  const [amount, setAmount] = useState(initialIncome ? String(initialIncome.amount) : "");
  const [sourceCode, setSourceCode] = useState(initialIncome?.source.code ?? "");
  const [incomeDate, setIncomeDate] = useState(initialIncome?.incomeDate ?? today());
  const [note, setNote] = useState(initialIncome?.note ?? "");

  const { data: sourcesData } = useQuery<GetSourcesResult>(GET_SOURCES);

  const [recordIncome] = useMutation<RecordIncomeResult, RecordIncomeVariables>(RECORD_INCOME, {
    refetchQueries: ["GetIncomes"],
  });
  const [editIncome] = useMutation<EditIncomeResult, EditIncomeVariables>(EDIT_INCOME, {
    refetchQueries: ["GetIncomes"],
  });

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();

    if (isEditing) {
      await editIncome({
        variables: {
          id: initialIncome.id,
          input: {
            amount: Number(amount),
            incomeDate,
            sourceCode,
            note: note.trim() === "" ? null : note,
          },
        },
      });
    } else {
      await recordIncome({
        variables: {
          input: {
            amount: Number(amount),
            incomeDate,
            sourceCode,
            note: note.trim() === "" ? null : note,
          },
        },
      });
      setAmount("");
      setSourceCode("");
      setIncomeDate(today());
      setNote("");
    }

    onSaved?.();
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="income-amount">Amount</Label>
          <Input
            id="income-amount"
            type="number"
            step="0.01"
            min="0.01"
            required
            value={amount}
            onChange={(event) => setAmount(event.target.value)}
          />
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="income-source">Source</Label>
          <select
            id="income-source"
            required
            value={sourceCode}
            onChange={(event) => setSourceCode(event.target.value)}
            className="h-8 w-full min-w-0 rounded-lg border border-input bg-transparent px-2.5 py-1 text-sm outline-none transition-colors focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30"
          >
            <option value="" disabled>
              Select a source
            </option>
            {sourcesData?.sources.map((source) => (
              <option key={source.code} value={source.code}>
                {source.label}
              </option>
            ))}
          </select>
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="income-date">Date</Label>
          <Input
            id="income-date"
            type="date"
            value={incomeDate}
            onChange={(event) => setIncomeDate(event.target.value)}
          />
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="income-note">Note</Label>
          <Textarea id="income-note" value={note} onChange={(event) => setNote(event.target.value)} />
        </div>
      </div>
      <div>
        <Button type="submit">{isEditing ? "Save changes" : "Record income"}</Button>
      </div>
    </form>
  );
}
