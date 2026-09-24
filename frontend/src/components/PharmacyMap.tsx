import L from "leaflet";
import { MapContainer, Marker, Popup, TileLayer } from "react-leaflet";
import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";
import type { Pharmacy } from "../api/pharmacies";
import "./PharmacyMap.css";

// react-leaflet's default marker icon URLs resolve relative to Leaflet's own CSS file, which
// breaks under Vite's bundling - re-pointing them at the bundled asset URLs is the standard fix.
delete (L.Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

export function PharmacyMap({
  pharmacies,
  center,
}: {
  pharmacies: Pharmacy[];
  center: { lat: number; lng: number };
}) {
  return (
    <MapContainer center={[center.lat, center.lng]} zoom={14} className="pharmacy-map" scrollWheelZoom={false}>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {pharmacies.map((pharmacy) => (
        <Marker key={pharmacy.id} position={[pharmacy.latitude, pharmacy.longitude]}>
          <Popup>{pharmacy.name}</Popup>
        </Marker>
      ))}
    </MapContainer>
  );
}
