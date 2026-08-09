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
import { Button } from "@/shared/components/ui/button";
import { Input } from "@/shared/components/ui/input";
import { Label } from "@/shared/components/ui/label";
import { Textarea } from "@/shared/components/ui/textarea";

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
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="expense-amount">Amount</Label>
          <Input
            id="expense-amount"
            type="number"
            step="0.01"
            min="0.01"
            required
            value={amount}
            onChange={(event) => setAmount(event.target.value)}
          />
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="expense-category">Category</Label>
          <select
            id="expense-category"
            required
            value={categoryCode}
            onChange={(event) => setCategoryCode(event.target.value)}
            className="h-8 w-full min-w-0 rounded-lg border border-input bg-transparent px-2.5 py-1 text-sm outline-none transition-colors focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30"
          >
            <option value="" disabled>
              Select a category
            </option>
            {categoriesData?.categories.map((category) => (
              <option key={category.code} value={category.code}>
                {category.label}
              </option>
            ))}
          </select>
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="expense-date">Date</Label>
          <Input
            id="expense-date"
            type="date"
            value={expenseDate}
            onChange={(event) => setExpenseDate(event.target.value)}
          />
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="expense-note">Note</Label>
          <Textarea id="expense-note" value={note} onChange={(event) => setNote(event.target.value)} />
        </div>
      </div>
      <div>
        <Button type="submit">{isEditing ? "Save changes" : "Record expense"}</Button>
      </div>
    </form>
  );
}
