import { useMutation } from "@tanstack/react-query";
import { apiSend } from "./client";
import type { Pharmacy } from "./pharmacies";

export interface AvailabilityRequestItem {
  drugId: number;
  quantity: number;
}

export interface AvailabilityCheckRequest {
  items: AvailabilityRequestItem[];
  latitude: number;
  longitude: number;
  radiusKm?: number;
}

export interface AvailabilityItemResult {
  drugId: number;
  drugName: string;
  form: string;
  packSize: string;
  requestedQuantity: number;
  inStock: boolean;
  price: number;
  lastUpdated: string | null;
}

export interface PharmacyAvailabilityResult {
  pharmacy: Pharmacy;
  allAvailable: boolean;
  items: AvailabilityItemResult[];
}

export function useAvailabilityCheckMutation() {
  return useMutation({
    mutationFn: (request: AvailabilityCheckRequest) =>
      apiSend<PharmacyAvailabilityResult[]>("POST", "/api/availability/check", request),
  });
}
