import { useQuery } from "@tanstack/react-query";
import { apiGet } from "./client";

export interface Prescription {
  drug: string;
  dosage: string;
  doctorName: string;
  issuedDate: string;
  status: "OPEN" | "REDEEMED";
}

export function usePrescriptionsQuery(svnr: string | undefined) {
  return useQuery({
    queryKey: ["prescriptions", svnr],
    queryFn: () => apiGet<Prescription[]>(`/api/oegk/${svnr}/prescriptions`),
    enabled: svnr !== undefined,
  });
}
