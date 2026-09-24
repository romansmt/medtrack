import { useMutation } from "@tanstack/react-query";
import { apiSend } from "./client";

export interface IdAustriaIdentity {
  fullName: string;
  dateOfBirth: string;
  authenticatedAt: string;
}

export interface ECardDetails {
  svnr: string;
  medtrackId: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  cardSerialNumber: string;
  carrierNumber: string;
  carrierName: string;
  expiryDate: string;
}

export function useIdAustriaLoginMutation() {
  return useMutation({
    mutationFn: (input: { fullName: string; dateOfBirth: string }) =>
      apiSend<IdAustriaIdentity>("POST", "/api/registration/id-austria-login", input),
  });
}

export function useScanCardMutation() {
  return useMutation({
    mutationFn: (fullName: string) =>
      apiSend<ECardDetails>("POST", "/api/registration/scan-card", { fullName }),
  });
}
