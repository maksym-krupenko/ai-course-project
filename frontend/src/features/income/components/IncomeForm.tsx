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
    <form onSubmit={handleSubmit}>
      <label>
        Amount
        <input
          type="number"
          step="0.01"
          min="0.01"
          required
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
        />
      </label>
      <label>
        Source
        <select required value={sourceCode} onChange={(event) => setSourceCode(event.target.value)}>
          <option value="" disabled>
            Select a source
          </option>
          {sourcesData?.sources.map((source) => (
            <option key={source.code} value={source.code}>
              {source.label}
            </option>
          ))}
        </select>
      </label>
      <label>
        Date
        <input type="date" value={incomeDate} onChange={(event) => setIncomeDate(event.target.value)} />
      </label>
      <label>
        Note
        <textarea value={note} onChange={(event) => setNote(event.target.value)} />
      </label>
      <button type="submit">{isEditing ? "Save changes" : "Record income"}</button>
    </form>
  );
}
