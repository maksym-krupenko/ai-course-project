import { useMutation, useQuery } from "@apollo/client";
import { type FormEvent, useState } from "react";

import {
  EDIT_EXPENSE,
  type EditExpenseResult,
  type EditExpenseVariables,
  type Expense,
  GET_CATEGORIES,
  type GetCategoriesResult,
  RECORD_EXPENSE,
  type RecordExpenseResult,
  type RecordExpenseVariables,
} from "@/features/expense/api/queries";

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

interface ExpenseFormProps {
  initialExpense?: Expense;
  onSaved?: () => void;
}

export function ExpenseForm({ initialExpense, onSaved }: ExpenseFormProps) {
  const isEditing = initialExpense !== undefined;
  const [amount, setAmount] = useState(initialExpense ? String(initialExpense.amount) : "");
  const [categoryCode, setCategoryCode] = useState(initialExpense?.category.code ?? "");
  const [expenseDate, setExpenseDate] = useState(initialExpense?.expenseDate ?? today());
  const [note, setNote] = useState(initialExpense?.note ?? "");

  const { data: categoriesData } = useQuery<GetCategoriesResult>(GET_CATEGORIES);

  const [recordExpense] = useMutation<RecordExpenseResult, RecordExpenseVariables>(RECORD_EXPENSE, {
    refetchQueries: ["GetExpenses"],
  });
  const [editExpense] = useMutation<EditExpenseResult, EditExpenseVariables>(EDIT_EXPENSE, {
    refetchQueries: ["GetExpenses"],
  });

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();

    if (isEditing) {
      await editExpense({
        variables: {
          id: initialExpense.id,
          input: {
            amount: Number(amount),
            expenseDate,
            categoryCode,
            note: note.trim() === "" ? null : note,
          },
        },
      });
    } else {
      await recordExpense({
        variables: {
          input: {
            amount: Number(amount),
            expenseDate,
            categoryCode,
            note: note.trim() === "" ? null : note,
          },
        },
      });
      setAmount("");
      setCategoryCode("");
      setExpenseDate(today());
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
        Category
        <select required value={categoryCode} onChange={(event) => setCategoryCode(event.target.value)}>
          <option value="" disabled>
            Select a category
          </option>
          {categoriesData?.categories.map((category) => (
            <option key={category.code} value={category.code}>
              {category.label}
            </option>
          ))}
        </select>
      </label>
      <label>
        Date
        <input type="date" value={expenseDate} onChange={(event) => setExpenseDate(event.target.value)} />
      </label>
      <label>
        Note
        <textarea value={note} onChange={(event) => setNote(event.target.value)} />
      </label>
      <button type="submit">{isEditing ? "Save changes" : "Record expense"}</button>
    </form>
  );
}
