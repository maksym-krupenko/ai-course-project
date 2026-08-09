import { BrowserRouter, Route, Routes } from "react-router-dom";

import { GraphQLProvider } from "@/app/providers/GraphQLProvider";
import { ExpensePage } from "@/features/expense/components/ExpensePage";
import { HealthStatus } from "@/features/health/components/HealthStatus";

export function App() {
  return (
    <GraphQLProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<ExpensePage />} />
          <Route path="/health" element={<HealthStatus />} />
        </Routes>
      </BrowserRouter>
    </GraphQLProvider>
  );
}
