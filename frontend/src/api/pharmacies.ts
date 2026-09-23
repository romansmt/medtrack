import { useMutation, useQuery } from "@tanstack/react-query";
import { apiGet } from "./client";

export interface Pharmacy {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  wheelchairAccessible: boolean;
  phone: string;
  email: string;
  website: string;
  reservationSupported: boolean;
  distanceKm: number | null;
  openNow: boolean;
  onCallNow: boolean;
}

export interface Geocoded {
  latitude: number;
  longitude: number;
}

export function useNearbyPharmaciesQuery(lat: number | undefined, lng: number | undefined) {
  return useQuery({
    queryKey: ["pharmacies", "nearby", lat, lng],
    queryFn: () => apiGet<Pharmacy[]>(`/api/pharmacies/nearby?lat=${lat}&lng=${lng}`),
    enabled: lat !== undefined && lng !== undefined,
  });
}

export function useGeocodeMutation() {
  return useMutation({
    mutationFn: (address: string) =>
      apiGet<Geocoded>(`/api/pharmacies/geocode?address=${encodeURIComponent(address)}`),
  });
}
