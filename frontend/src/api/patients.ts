import { useQuery } from "@tanstack/react-query";
import { apiGet } from "./client";

export interface Patient {
  id: number;
  svnr: string;
  name: string;
}

export function usePatientsQuery() {
  return useQuery({
    queryKey: ["patients"],
    queryFn: () => apiGet<Patient[]>("/api/patients"),
    staleTime: 5 * 60 * 1000,
  });
}
