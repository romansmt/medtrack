import { useState } from "react";
import { useGeocodeMutation } from "../api/pharmacies";
import { useUserLocation } from "../context/UserLocationContext";
import "./LocationPicker.css";

export function LocationPicker() {
  const { location, setLocation } = useUserLocation();
  const [editing, setEditing] = useState(!location);
  const [address, setAddress] = useState("");
  const geocode = useGeocodeMutation();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!address.trim()) return;
    geocode.mutate(address.trim(), {
      onSuccess: (coords) => {
        setLocation({ label: address.trim(), lat: coords.latitude, lng: coords.longitude });
        setEditing(false);
        setAddress("");
      },
    });
  };

  if (!editing && location) {
    return (
      <div className="location-picker">
        <span className="location-picker__label">Standort: {location.label}</span>
        <button type="button" className="location-picker__change" onClick={() => setEditing(true)}>
          Ändern
        </button>
      </div>
    );
  }

  return (
    <form className="location-picker location-picker--form" onSubmit={handleSubmit}>
      <input
        type="text"
        value={address}
        onChange={(e) => setAddress(e.target.value)}
        placeholder="Adresse eingeben, z. B. Freyung 7, Wien"
        aria-label="Standort festlegen"
      />
      <button type="submit" disabled={geocode.isPending || !address.trim()}>
        {geocode.isPending ? "Suche…" : "Standort festlegen"}
      </button>
      {location && (
        <button type="button" className="location-picker__cancel" onClick={() => setEditing(false)}>
          Abbrechen
        </button>
      )}
      {geocode.isError && <p className="location-picker__error">Adresse konnte nicht gefunden werden.</p>}
    </form>
  );
}
