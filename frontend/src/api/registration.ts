import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiSend } from "./client";

export interface IdAustriaIdentity {
  fullName: string;
  dateOfBirth: string;
  authenticatedAt: string;
}

export interface ECardDetails {
  svnr: string;
  medtrackId: string | null;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  cardSerialNumber: string;
  carrierNumber: string;
  carrierName: string;
  expiryDate: string;
}

// What ConfirmCardDataStep submits for a real ID-Austria comparison - everything but medtrackId,
// which is assigned by MedTrack, not asserted by the user.
export type VerifyIdentityInput = Omit<ECardDetails, "medtrackId">;

export interface VerifyIdentityResult {
  verified: ECardDetails;
  alreadyRegistered: boolean;
}

// A logged-in/just-registered account summary - deliberately smaller than ECardDetails, since only
// the svnr is ever used afterward (to select the active patient).
export interface Account {
  svnr: string;
  medtrackId: string;
  name: string;
  email: string | null;
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

// The real ID-Austria comparison: throws (via apiSend) with a specific message on a wrong SVNR or a
// mismatched field list. `alreadyRegistered` tells the caller which follow-up step to take.
export function useVerifyIdentityMutation() {
  return useMutation({
    mutationFn: (input: VerifyIdentityInput) =>
      apiSend<VerifyIdentityResult>("POST", "/api/registration/verify-identity", input),
  });
}

// Only called after verify-identity succeeded with alreadyRegistered=false - sets the email/password
// that becomes this account's Standard-login credential.
export function useCompleteRegistrationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: { svnr: string; email: string; password: string }) =>
      apiSend<Account>("POST", "/api/registration/complete-registration", input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["patients"] });
    },
  });
}

// Standard (non-ID-Austria) login: Name, Surname, Email and Password compared against the account
// created during registration. The only login method - see AuthEntryStep.
export function useStandardLoginMutation() {
  return useMutation({
    mutationFn: (input: { firstName: string; lastName: string; email: string; password: string }) =>
      apiSend<Account>("POST", "/api/registration/standard-login", input),
  });
}
