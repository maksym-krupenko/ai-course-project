import type { ReactNode } from "react";
import { BrowserRouter, NavLink, Route, Routes } from "react-router-dom";

import { GraphQLProvider } from "@/app/providers/GraphQLProvider";
import { ExpensePage } from "@/features/expense/components/ExpensePage";
import { HealthStatus } from "@/features/health/components/HealthStatus";
import { IncomePage } from "@/features/income/components/IncomePage";
import { cn } from "@/shared/utils/cn";

function NavItem({ to, children }: { to: string; children: ReactNode }) {
  return (
    <NavLink
      to={to}
      end
      className={({ isActive }) =>
        cn(
          "rounded-lg px-3 py-1.5 text-sm font-medium transition-colors",
          isActive ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-muted hover:text-foreground",
        )
      }
    >
      {children}
    </NavLink>
  );
}

export function App() {
  return (
    <GraphQLProvider>
      <BrowserRouter>
        <div className="min-h-screen bg-background text-foreground">
          <header className="border-b border-border">
            <div className="mx-auto flex max-w-3xl items-center gap-6 px-4 py-4">
              <span className="font-heading text-lg font-semibold">Finance App</span>
              <nav className="flex gap-1">
                <NavItem to="/">Expenses</NavItem>
                <NavItem to="/income">Income</NavItem>
              </nav>
            </div>
          </header>
          <main className="mx-auto max-w-3xl px-4 py-8">
            <Routes>
              <Route path="/" element={<ExpensePage />} />
              <Route path="/income" element={<IncomePage />} />
              <Route path="/health" element={<HealthStatus />} />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </GraphQLProvider>
  );
}
