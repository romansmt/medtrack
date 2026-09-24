import { useQuery } from "@tanstack/react-query";
import { apiGet } from "./client";
import type { Pharmacy } from "./pharmacies";

export interface Drug {
  id: number;
  name: string;
  activeSubstance: string;
  form: string;
  packSize: string;
  manufacturer: string;
  prescriptionRequired: boolean;
  pzn: string;
}

export interface DrugLeaflet {
  drugId: number;
  drugName: string;
  leafletText: string;
}

export interface PriceComparisonEntry {
  pharmacy: Pharmacy;
  price: number;
  inStock: boolean;
}

export function useDrugSearchQuery(q: string) {
  return useQuery({
    queryKey: ["drugs", "search", q],
    queryFn: () => apiGet<Drug[]>(`/api/drugs/search?q=${encodeURIComponent(q)}`),
    enabled: q.trim().length > 0,
  });
}

export function useDrugQuery(id: number | undefined) {
  return useQuery({
    queryKey: ["drugs", id],
    queryFn: () => apiGet<Drug>(`/api/drugs/${id}`),
    enabled: id !== undefined,
  });
}

export function useDrugLeafletQuery(id: number | undefined) {
  return useQuery({
    queryKey: ["drugs", id, "leaflet"],
    queryFn: () => apiGet<DrugLeaflet>(`/api/drugs/${id}/leaflet`),
    enabled: id !== undefined,
  });
}

export function usePriceComparisonQuery(id: number | undefined, lat: number | undefined, lng: number | undefined) {
  return useQuery({
    queryKey: ["drugs", id, "compare", lat, lng],
    queryFn: () => apiGet<PriceComparisonEntry[]>(`/api/drugs/${id}/compare?lat=${lat}&lng=${lng}`),
    enabled: id !== undefined && lat !== undefined && lng !== undefined,
  });
}
