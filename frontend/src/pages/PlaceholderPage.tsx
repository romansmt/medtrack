export function PlaceholderPage({ title, task }: { title: string; task: string }) {
  return (
    <section>
      <h1>{title}</h1>
      <p style={{ color: "var(--color-text-muted)" }}>Wird in {task} umgesetzt.</p>
    </section>
  );
}
