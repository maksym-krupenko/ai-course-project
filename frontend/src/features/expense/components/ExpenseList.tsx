import { useMutation } from "@apollo/client";

import {
  DELETE_EXPENSE,
  type DeleteExpenseResult,
  type DeleteExpenseVariables,
  type Expense,
} from "@/features/expense/api/queries";
import { Button } from "@/shared/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/shared/components/ui/table";

interface ExpenseRowProps {
  expense: Expense;
  onEdit: (expense: Expense) => void;
}

function ExpenseRow({ expense, onEdit }: ExpenseRowProps) {
  const [deleteExpense] = useMutation<DeleteExpenseResult, DeleteExpenseVariables>(DELETE_EXPENSE, {
    variables: { id: expense.id },
    update: (cache, { data }) => {
      if (!data) return;
      cache.evict({ id: cache.identify({ __typename: "Expense", id: data.deleteExpense }) });
      cache.gc();
    },
  });

  return (
    <TableRow>
      <TableCell>{expense.expenseDate}</TableCell>
      <TableCell>{expense.category.label}</TableCell>
      <TableCell>
        {expense.amount} {expense.currency}
      </TableCell>
      <TableCell className="text-muted-foreground">{expense.note}</TableCell>
      <TableCell>
        <div className="flex gap-2">
          <Button type="button" variant="outline" size="sm" onClick={() => onEdit(expense)}>
            Edit
          </Button>
          <Button type="button" variant="destructive" size="sm" onClick={() => deleteExpense()}>
            Delete
          </Button>
        </div>
      </TableCell>
    </TableRow>
  );
}

interface ExpenseListProps {
  expenses: Expense[];
  onEdit: (expense: Expense) => void;
}

export function ExpenseList({ expenses, onEdit }: ExpenseListProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Date</TableHead>
          <TableHead>Category</TableHead>
          <TableHead>Amount</TableHead>
          <TableHead>Note</TableHead>
          <TableHead>Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {expenses.map((expense) => (
          <ExpenseRow key={expense.id} expense={expense} onEdit={onEdit} />
        ))}
      </TableBody>
    </Table>
  );
}
