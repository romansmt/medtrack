import type { IconName } from "../layout/Icon";

export interface OnboardingSlide {
  icon: IconName;
  title: string;
  body: string;
}

export const introSlides: OnboardingSlide[] = [
  {
    icon: "store",
    title: "Apotheken in Ihrer Nähe finden",
    body: "Finden Sie schnell geöffnete Apotheken in Ihrer Umgebung - inklusive Nachtdienst- und Bereitschaftszeiten.",
  },
  {
    icon: "search",
    title: "Direkter Weg zu Ihren Medikamenten",
    body: "Suchen Sie gezielt nach einem Medikament. MedTrack zeigt Ihnen, welche Apotheke es gerade vorrätig hat.",
  },
  {
    icon: "pill",
    title: "Ihr Einnahmeplan immer im Blick",
    body: "Behalten Sie den Überblick über Ihre Medikamente - mit Erinnerungen und einer einfachen Bestätigung der Einnahme.",
  },
];

export const consentSlide: OnboardingSlide = {
  icon: "shield",
  title: "Sicherheit und Transparenz haben oberste Priorität",
  body: "MedTrack ist ein Demo-/Portfolio-Projekt und simuliert das österreichische Gesundheitssystem (e-card, ELGA e-Medikation). Es ist keine echte Behörden- oder Gesundheitsanwendung. Alle Patienten, Rezepte, Apotheken und sonstigen Daten sind vollständig fiktiv und wurden von der App selbst erzeugt - es werden keine echten persönlichen oder Gesundheitsdaten erfasst, verarbeitet oder gespeichert.",
};
