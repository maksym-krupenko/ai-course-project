import { gql } from "@apollo/client";

export interface Source {
  code: string;
  label: string;
}

export interface Income {
  id: string;
  amount: number;
  currency: string;
  incomeDate: string;
  source: Source;
  note: string | null;
}

export interface GetIncomesResult {
  incomes: Income[];
}

export interface GetIncomesVariables {
  from: string;
  to: string;
}

export const GET_INCOMES = gql`
  query GetIncomes($from: LocalDate!, $to: LocalDate!) {
    incomes(from: $from, to: $to) {
      id
      amount
      currency
      incomeDate
      source {
        code
        label
      }
      note
    }
  }
`;

export interface GetSourcesResult {
  sources: Source[];
}

export const GET_SOURCES = gql`
  query GetSources {
    sources {
      code
      label
    }
  }
`;

export interface RecordIncomeInput {
  amount: number;
  incomeDate?: string | null;
  sourceCode: string;
  note?: string | null;
}

export interface RecordIncomeResult {
  recordIncome: Income;
}

export interface RecordIncomeVariables {
  input: RecordIncomeInput;
}

export const RECORD_INCOME = gql`
  mutation RecordIncome($input: RecordIncomeInput!) {
    recordIncome(input: $input) {
      id
      amount
      currency
      incomeDate
      source {
        code
        label
      }
      note
    }
  }
`;

export interface EditIncomeInput {
  amount: number;
  incomeDate: string;
  sourceCode: string;
  note?: string | null;
}

export interface EditIncomeResult {
  editIncome: Income;
}

export interface EditIncomeVariables {
  id: string;
  input: EditIncomeInput;
}

export const EDIT_INCOME = gql`
  mutation EditIncome($id: ID!, $input: EditIncomeInput!) {
    editIncome(id: $id, input: $input) {
      id
      amount
      currency
      incomeDate
      source {
        code
        label
      }
      note
    }
  }
`;

export interface DeleteIncomeResult {
  deleteIncome: string;
}

export interface DeleteIncomeVariables {
  id: string;
}

export const DELETE_INCOME = gql`
  mutation DeleteIncome($id: ID!) {
    deleteIncome(id: $id)
  }
`;
