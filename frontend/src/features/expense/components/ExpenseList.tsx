import { useMutation } from "@apollo/client";

import {
  DELETE_EXPENSE,
  type DeleteExpenseResult,
  type DeleteExpenseVariables,
  type Expense,
} from "@/features/expense/api/queries";

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
    <tr>
      <td>{expense.expenseDate}</td>
      <td>{expense.category.label}</td>
      <td>
        {expense.amount} {expense.currency}
      </td>
      <td>{expense.note}</td>
      <td>
        <button type="button" onClick={() => onEdit(expense)}>
          Edit
        </button>
        <button type="button" onClick={() => deleteExpense()}>
          Delete
        </button>
      </td>
    </tr>
  );
}

interface ExpenseListProps {
  expenses: Expense[];
  onEdit: (expense: Expense) => void;
}

export function ExpenseList({ expenses, onEdit }: ExpenseListProps) {
  return (
    <table>
      <thead>
        <tr>
          <th>Date</th>
          <th>Category</th>
          <th>Amount</th>
          <th>Note</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {expenses.map((expense) => (
          <ExpenseRow key={expense.id} expense={expense} onEdit={onEdit} />
        ))}
      </tbody>
    </table>
  );
}
