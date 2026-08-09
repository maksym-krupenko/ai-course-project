interface PeriodFilterProps {
  from: string;
  to: string;
  onChange: (from: string, to: string) => void;
}

export function PeriodFilter({ from, to, onChange }: PeriodFilterProps) {
  return (
    <fieldset>
      <legend>Period</legend>
      <label>
        From
        <input type="date" value={from} onChange={(event) => onChange(event.target.value, to)} />
      </label>
      <label>
        To
        <input type="date" value={to} onChange={(event) => onChange(from, event.target.value)} />
      </label>
    </fieldset>
  );
}
