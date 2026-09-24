import { useState } from "react";
import { useDrugSearchQuery, type Drug } from "../api/drugs";
import "./DrugSearchCombobox.css";

export function DrugSearchCombobox({
  onSelect,
  placeholder = "Medikament suchen…",
}: {
  onSelect: (drug: Drug) => void;
  placeholder?: string;
}) {
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);
  const { data, isFetching } = useDrugSearchQuery(query);

  const handleSelect = (drug: Drug) => {
    onSelect(drug);
    setQuery("");
    setOpen(false);
  };

  return (
    <div className="drug-combobox">
      <input
        type="text"
        value={query}
        placeholder={placeholder}
        onChange={(e) => {
          setQuery(e.target.value);
          setOpen(true);
        }}
        onFocus={() => setOpen(true)}
        onBlur={() => setTimeout(() => setOpen(false), 150)}
      />
      {open && query.trim() && (
        <ul className="drug-combobox__results">
          {isFetching && <li className="drug-combobox__status">Suche…</li>}
          {!isFetching && data?.length === 0 && <li className="drug-combobox__status">Keine Treffer</li>}
          {data?.map((drug) => (
            <li key={drug.id}>
              <button type="button" onMouseDown={() => handleSelect(drug)}>
                <span className="drug-combobox__name">{drug.name}</span>
                <span className="drug-combobox__meta">
                  {drug.form}, {drug.packSize}
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
