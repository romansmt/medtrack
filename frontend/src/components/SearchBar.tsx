import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Icon } from "../layout/Icon";
import "./SearchBar.css";

export function SearchBar() {
  const [query, setQuery] = useState("");
  const navigate = useNavigate();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const params = query.trim() ? `?q=${encodeURIComponent(query.trim())}` : "";
    navigate(`/search${params}`);
  };

  return (
    <form className="search-bar" onSubmit={handleSubmit}>
      <Icon name="search" size={18} />
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Medikamente, Apotheken…"
        aria-label="Suche"
      />
    </form>
  );
}
