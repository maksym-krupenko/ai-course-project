import { useMutation } from "@apollo/client";

import {
  DELETE_INCOME,
  type DeleteIncomeResult,
  type DeleteIncomeVariables,
  type Income,
} from "@/features/income/api/queries";

interface IncomeRowProps {
  income: Income;
  onEdit: (income: Income) => void;
}

function IncomeRow({ income, onEdit }: IncomeRowProps) {
  const [deleteIncome] = useMutation<DeleteIncomeResult, DeleteIncomeVariables>(DELETE_INCOME, {
    variables: { id: income.id },
    update: (cache, { data }) => {
      if (!data) return;
      cache.evict({ id: cache.identify({ __typename: "Income", id: data.deleteIncome }) });
      cache.gc();
    },
  });

  return (
    <tr>
      <td>{income.incomeDate}</td>
      <td>{income.source.label}</td>
      <td>
        {income.amount} {income.currency}
      </td>
      <td>{income.note}</td>
      <td>
        <button type="button" onClick={() => onEdit(income)}>
          Edit
        </button>
        <button type="button" onClick={() => deleteIncome()}>
          Delete
        </button>
      </td>
    </tr>
  );
}

interface IncomeListProps {
  incomes: Income[];
  onEdit: (income: Income) => void;
}

export function IncomeList({ incomes, onEdit }: IncomeListProps) {
  return (
    <table>
      <thead>
        <tr>
          <th>Date</th>
          <th>Source</th>
          <th>Amount</th>
          <th>Note</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {incomes.map((income) => (
          <IncomeRow key={income.id} income={income} onEdit={onEdit} />
        ))}
      </tbody>
    </table>
  );
}
