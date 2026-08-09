import { BrowserRouter, Link, Route, Routes } from "react-router-dom";

import { GraphQLProvider } from "@/app/providers/GraphQLProvider";
import { ExpensePage } from "@/features/expense/components/ExpensePage";
import { HealthStatus } from "@/features/health/components/HealthStatus";
import { IncomePage } from "@/features/income/components/IncomePage";

export function App() {
  return (
    <GraphQLProvider>
      <BrowserRouter>
        <nav>
          <Link to="/">Expenses</Link>
          <Link to="/income">Income</Link>
        </nav>
        <Routes>
          <Route path="/" element={<ExpensePage />} />
          <Route path="/income" element={<IncomePage />} />
          <Route path="/health" element={<HealthStatus />} />
        </Routes>
      </BrowserRouter>
    </GraphQLProvider>
  );
}
