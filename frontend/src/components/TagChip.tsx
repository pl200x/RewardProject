interface Props {
  label: string
  selected?: boolean
  onClick?: () => void
}

/** Clickable preset interest tag, also used for display. Selected state is highlighted. */
export default function TagChip({ label, selected, onClick }: Props) {
  return (
    <button
      type="button"
      className={`mm-chip${selected ? ' mm-chip--on' : ''}`}
      aria-pressed={!!selected}
      onClick={onClick}
    >
      {label}
    </button>
  )
}
