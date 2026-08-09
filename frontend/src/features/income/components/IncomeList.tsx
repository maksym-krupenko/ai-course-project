import { useMutation } from "@apollo/client";

import {
  DELETE_INCOME,
  type DeleteIncomeResult,
  type DeleteIncomeVariables,
  type Income,
} from "@/features/income/api/queries";
import { Button } from "@/shared/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/shared/components/ui/table";

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
    <TableRow>
      <TableCell>{income.incomeDate}</TableCell>
      <TableCell>{income.source.label}</TableCell>
      <TableCell>
        {income.amount} {income.currency}
      </TableCell>
      <TableCell className="text-muted-foreground">{income.note}</TableCell>
      <TableCell>
        <div className="flex gap-2">
          <Button type="button" variant="outline" size="sm" onClick={() => onEdit(income)}>
            Edit
          </Button>
          <Button type="button" variant="destructive" size="sm" onClick={() => deleteIncome()}>
            Delete
          </Button>
        </div>
      </TableCell>
    </TableRow>
  );
}

interface IncomeListProps {
  incomes: Income[];
  onEdit: (income: Income) => void;
}

export function IncomeList({ incomes, onEdit }: IncomeListProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Date</TableHead>
          <TableHead>Source</TableHead>
          <TableHead>Amount</TableHead>
          <TableHead>Note</TableHead>
          <TableHead>Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {incomes.map((income) => (
          <IncomeRow key={income.id} income={income} onEdit={onEdit} />
        ))}
      </TableBody>
    </Table>
  );
}
