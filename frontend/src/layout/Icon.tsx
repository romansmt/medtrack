export type IconName =
  | "home"
  | "search"
  | "pill"
  | "store"
  | "heart"
  | "bookmark"
  | "doc"
  | "more"
  | "shield"
  | "camera"
  | "user"
  | "settings"
  | "feedback"
  | "info"
  | "help"
  | "chevron";

const paths: Record<IconName, string> = {
  home: "M3 11.5 12 4l9 7.5M5 10v9a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1v-9",
  search: "M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16Zm9 2-4.35-4.35",
  pill: "M8.5 15.5 15.5 8.5a4.95 4.95 0 1 1 7 7l-7 7a4.95 4.95 0 0 1-7-7Zm2.5 0 5 5",
  store: "M4 10v9a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-9M3 5h18l1.2 4.2a1.8 1.8 0 0 1-1.7 2.3 1.9 1.9 0 0 1-1.9-1.6 1.9 1.9 0 0 1-1.9 1.6 1.9 1.9 0 0 1-1.9-1.6 1.9 1.9 0 0 1-1.9 1.6 1.9 1.9 0 0 1-1.9-1.6 1.9 1.9 0 0 1-1.9 1.6 1.9 1.9 0 0 1-1.9-1.6 1.9 1.9 0 0 1-1.9 1.6A1.8 1.8 0 0 1 1.8 9.2Z",
  heart: "M20.8 8.6c0 4.5-8.8 10.2-8.8 10.2S3.2 13.1 3.2 8.6a4.8 4.8 0 0 1 8.8-2.6 4.8 4.8 0 0 1 8.8 2.6Z",
  bookmark: "M6 3h12a1 1 0 0 1 1 1v17l-7-4-7 4V4a1 1 0 0 1 1-1Z",
  doc: "M7 3h7l5 5v13a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Zm7 0v5h5M9 13h6M9 17h6",
  more: "M5 12h.01M12 12h.01M19 12h.01",
  shield: "M12 3 4 6v6c0 5 3.5 8.5 8 9 4.5-.5 8-4 8-9V6l-8-3Zm-2.5 9 2 2 4-4.5",
  camera: "M4 8h3l2-3h6l2 3h3a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9a1 1 0 0 1 1-1Zm8 9a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z",
  user: "M9 8a3 3 0 1 0 6 0a3 3 0 1 0-6 0Z M5 20a7 7 0 0 1 14 0",
  settings: "M9 12a3 3 0 1 0 6 0a3 3 0 1 0-6 0Z M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M18.4 5.6l-2.1 2.1M7.7 16.3l-2.1 2.1",
  feedback: "M4 5h16a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1H9l-4 4v-4H4a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1Z",
  info: "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18Z M12 11v6M12 7.5v.01",
  help: "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18Z M9.5 9a2.5 2.5 0 0 1 5 0c0 1.5-2.5 1.8-2.5 3.5M12 17v.01",
  chevron: "M9 6l6 6-6 6",
};

export function Icon({ name, size = 22 }: { name: IconName; size?: number }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={paths[name]} />
    </svg>
  );
}
