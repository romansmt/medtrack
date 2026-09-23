import { createContext, useContext, useState, type ReactNode } from "react";

const STORAGE_KEY = "medtrack.userLocation";

export interface UserLocation {
  label: string;
  lat: number;
  lng: number;
}

interface UserLocationContextValue {
  location: UserLocation | null;
  setLocation: (location: UserLocation) => void;
}

// Named UserLocation, not Location, to avoid colliding with react-router-dom's own useLocation (current URL, unrelated).
const UserLocationContext = createContext<UserLocationContextValue | undefined>(undefined);

function readStoredLocation(): UserLocation | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as UserLocation) : null;
  } catch {
    return null;
  }
}

export function UserLocationProvider({ children }: { children: ReactNode }) {
  const [location, setLocationState] = useState<UserLocation | null>(readStoredLocation);

  const setLocation = (next: UserLocation) => {
    setLocationState(next);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
    } catch {
      // ignore storage failures (e.g. private browsing) - location still holds for this session
    }
  };

  return (
    <UserLocationContext.Provider value={{ location, setLocation }}>
      {children}
    </UserLocationContext.Provider>
  );
}

export function useUserLocation(): UserLocationContextValue {
  const ctx = useContext(UserLocationContext);
  if (!ctx) {
    throw new Error("useUserLocation must be used within a UserLocationProvider");
  }
  return ctx;
}
