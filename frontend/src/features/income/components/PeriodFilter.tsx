import { Input } from "@/shared/components/ui/input";
import { Label } from "@/shared/components/ui/label";

interface PeriodFilterProps {
  from: string;
  to: string;
  onChange: (from: string, to: string) => void;
}

export function PeriodFilter({ from, to, onChange }: PeriodFilterProps) {
  return (
    <fieldset className="flex flex-wrap items-end gap-4">
      <legend className="mb-2 text-sm font-medium text-muted-foreground">Period</legend>
      <div className="flex flex-col gap-1.5">
        <Label htmlFor="period-from">From</Label>
        <Input
          id="period-from"
          type="date"
          value={from}
          onChange={(event) => onChange(event.target.value, to)}
        />
      </div>
      <div className="flex flex-col gap-1.5">
        <Label htmlFor="period-to">To</Label>
        <Input id="period-to" type="date" value={to} onChange={(event) => onChange(from, event.target.value)} />
      </div>
    </fieldset>
  );
}
