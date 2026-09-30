const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  // The raw response body text, when the server sent one (e.g. ApiExceptionHandler's
  // ResponseEntity<String> bodies - the specific "wrong SVNR" / mismatched-field messages this
  // project's registration flow relies on). Empty string if the response had no body.
  body: string;

  constructor(message: string, status: number, body: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

async function toApiError(method: string, path: string, response: Response): Promise<ApiError> {
  const body = await response.text().catch(() => "");
  const message = body.trim() !== "" ? body : `${method} ${path} failed with ${response.status}`;
  return new ApiError(message, response.status, body);
}

export async function apiGet<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`);
  if (!response.ok) {
    throw await toApiError("GET", path, response);
  }
  return response.json() as Promise<T>;
}

export async function apiSend<T>(
  method: "POST" | "PUT" | "DELETE",
  path: string,
  body?: unknown,
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: body === undefined ? undefined : { "Content-Type": "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!response.ok) {
    throw await toApiError(method, path, response);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}
