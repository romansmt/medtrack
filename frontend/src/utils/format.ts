const GERMAN_DAY_NAMES: Record<string, string> = {
  MONDAY: "Montag",
  TUESDAY: "Dienstag",
  WEDNESDAY: "Mittwoch",
  THURSDAY: "Donnerstag",
  FRIDAY: "Freitag",
  SATURDAY: "Samstag",
  SUNDAY: "Sonntag",
};

// Backend LocalDate/LocalTime fields serialize as ISO strings ("HH:mm:ss", "yyyy-MM-dd") - these
// helpers only ever need to slice/relabel them, never parse into Date objects (timezone-free).
export function germanDayName(dayOfWeek: string): string {
  return GERMAN_DAY_NAMES[dayOfWeek] ?? dayOfWeek;
}

export function formatTime(isoTime: string): string {
  return isoTime.slice(0, 5);
}

export function formatDistance(distanceKm: number | null): string {
  if (distanceKm === null) return "";
  return distanceKm < 1 ? `${Math.round(distanceKm * 1000)} m entfernt` : `${distanceKm.toFixed(1)} km entfernt`;
}

export function formatPrice(price: number): string {
  return `${price.toFixed(2).replace(".", ",")} €`;
}

export function formatDateTime(isoInstant: string): string {
  const date = new Date(isoInstant);
  return date.toLocaleString("de-AT", { dateStyle: "short", timeStyle: "short" });
}

export function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split("-");
  return `${day}.${month}.${year}`;
}
