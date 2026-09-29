import { useMutation, useQueryClient } from "@tanstack/react-query";
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

// Registers a brand-new MedTrack account (not one of the 4 pre-seeded demo patients). Invalidates
// the patients list so the header's dropdown includes the new account immediately, without a
// full page reload. Shared by both the ID-Austria "Registrieren" tab (email omitted) and standard
// registration (email set) - see CreateAccountRequest on the backend.
export function useCreateAccountMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: { fullName: string; dateOfBirth: string; email?: string }) =>
      apiSend<ECardDetails>("POST", "/api/registration/create-account", input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["patients"] });
    },
  });
}

// Standard (non-ID-Austria) login by email - no password check happens anywhere, see
// RegistrationController. Returns the same ECardDetails shape as the other flows so the caller can
// selectPatient(details.svnr), but the UI never shows the e-card-specific fields for this path.
export function useStandardLoginMutation() {
  return useMutation({
    mutationFn: (email: string) => apiSend<ECardDetails>("POST", "/api/registration/standard-login", { email }),
  });
}
