import type { IconName } from "./Icon";

export interface NavItem {
  to: string;
  label: string;
  icon: IconName;
}

export const primaryNavItems: NavItem[] = [
  { to: "/", label: "Home", icon: "home" },
  { to: "/search", label: "Suche", icon: "search" },
  { to: "/medication-plan", label: "Einnahmeplan", icon: "pill" },
];

export const secondaryNavItems: NavItem[] = [
  { to: "/pharmacies", label: "Apotheken", icon: "store" },
  { to: "/favorites", label: "Favoriten", icon: "heart" },
  { to: "/reservations", label: "Reservierungen", icon: "bookmark" },
  { to: "/prescriptions", label: "Rezepte", icon: "doc" },
];

export const allNavItems: NavItem[] = [...primaryNavItems, ...secondaryNavItems];
