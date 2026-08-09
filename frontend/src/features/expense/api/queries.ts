import { gql } from "@apollo/client";

export interface Category {
  code: string;
  label: string;
}

export interface Expense {
  id: string;
  amount: number;
  currency: string;
  expenseDate: string;
  category: Category;
  note: string | null;
}

export interface GetExpensesResult {
  expenses: Expense[];
}

export interface GetExpensesVariables {
  from: string;
  to: string;
}

export const GET_EXPENSES = gql`
  query GetExpenses($from: LocalDate!, $to: LocalDate!) {
    expenses(from: $from, to: $to) {
      id
      amount
      currency
      expenseDate
      category {
        code
        label
      }
      note
    }
  }
`;

export interface GetCategoriesResult {
  categories: Category[];
}

export const GET_CATEGORIES = gql`
  query GetCategories {
    categories {
      code
      label
    }
  }
`;

export interface RecordExpenseInput {
  amount: number;
  expenseDate?: string | null;
  categoryCode: string;
  note?: string | null;
}

export interface RecordExpenseResult {
  recordExpense: Expense;
}

export interface RecordExpenseVariables {
  input: RecordExpenseInput;
}

export const RECORD_EXPENSE = gql`
  mutation RecordExpense($input: RecordExpenseInput!) {
    recordExpense(input: $input) {
      id
      amount
      currency
      expenseDate
      category {
        code
        label
      }
      note
    }
  }
`;

export interface EditExpenseInput {
  amount: number;
  expenseDate: string;
  categoryCode: string;
  note?: string | null;
}

export interface EditExpenseResult {
  editExpense: Expense;
}

export interface EditExpenseVariables {
  id: string;
  input: EditExpenseInput;
}

export const EDIT_EXPENSE = gql`
  mutation EditExpense($id: ID!, $input: EditExpenseInput!) {
    editExpense(id: $id, input: $input) {
      id
      amount
      currency
      expenseDate
      category {
        code
        label
      }
      note
    }
  }
`;

export interface DeleteExpenseResult {
  deleteExpense: string;
}

export interface DeleteExpenseVariables {
  id: string;
}

export const DELETE_EXPENSE = gql`
  mutation DeleteExpense($id: ID!) {
    deleteExpense(id: $id)
  }
`;
