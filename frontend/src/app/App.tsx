import { BrowserRouter, Route, Routes } from "react-router-dom";

import { GraphQLProvider } from "@/app/providers/GraphQLProvider";
import { HealthStatus } from "@/features/health/components/HealthStatus";

export function App() {
  return (
    <GraphQLProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HealthStatus />} />
        </Routes>
      </BrowserRouter>
    </GraphQLProvider>
  );
}
