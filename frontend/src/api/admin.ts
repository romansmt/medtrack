import { useMutation } from "@tanstack/react-query";
import { apiSend } from "./client";

// Resolves on 204 (correct code), throws ApiError with status 401 on an incorrect one - see
// AdminController on the backend.
export function useVerifyAdminCodeMutation() {
  return useMutation({
    mutationFn: (code: string) => apiSend<void>("POST", "/api/admin/verify-code", { code }),
  });
}
