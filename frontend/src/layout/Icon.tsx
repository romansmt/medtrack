export type IconName =
  | "home"
  | "search"
  | "pill"
  | "store"
  | "heart"
  | "bookmark"
  | "doc"
  | "more"
  | "shield";

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
