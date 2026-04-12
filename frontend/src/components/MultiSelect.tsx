import { useState, useEffect, useRef } from 'react';
import type { MetadataItem } from '../types';
import './MultiSelect.css';

export default function MultiSelect({
  label,
  options,
  selected,
  onChange,
  searchable = false,
  grouped,
}: {
  label: string;
  options: MetadataItem[] | null;
  selected: number[];
  onChange: (ids: number[]) => void;
  searchable?: boolean;
  grouped?: { label: string; ids: number[] }[];
}) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const ref = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (open && searchable) searchRef.current?.focus();
    if (!open) setSearch('');
  }, [open, searchable]);

  function toggle(id: number) {
    onChange(selected.includes(id) ? selected.filter(x => x !== id) : [...selected, id]);
  }

  const filtered = options?.filter(
    opt => !search || opt.name.toLowerCase().includes(search.toLowerCase()),
  );

  const summary = selected.length === 0 ? 'Any' : `${selected.length} selected`;

  function renderOptions(items: MetadataItem[]) {
    return items.map(opt => (
      <label key={opt.id} className="multi-select-option">
        <input
          type="checkbox"
          checked={selected.includes(opt.id)}
          onChange={() => toggle(opt.id)}
        />
        {opt.name}
      </label>
    ));
  }

  return (
    <div className="multi-select" ref={ref}>
      <span className="filter-group-label">{label}</span>
      <button
        type="button"
        className="multi-select-trigger"
        onClick={() => setOpen(o => !o)}
        aria-expanded={open}
      >
        {summary}
        <span className="multi-select-chevron" aria-hidden>▾</span>
      </button>
      {open && (
        <div className="multi-select-dropdown" role="listbox" aria-multiselectable aria-label={label}>
          {searchable && (
            <input
              ref={searchRef}
              className="multi-select-search"
              type="text"
              placeholder="Search…"
              value={search}
              onChange={e => setSearch(e.target.value)}
              onClick={e => e.stopPropagation()}
            />
          )}
          {options === null && <p className="multi-select-empty">Loading…</p>}
          {filtered !== undefined && filtered.length === 0 && (
            <p className="multi-select-empty">{search ? 'No matches' : 'None available'}</p>
          )}
          {grouped && !search ? (
            grouped.map(group => {
              const groupOpts = (filtered ?? []).filter(o => group.ids.includes(o.id));
              if (groupOpts.length === 0) return null;
              return (
                <div key={group.label} className="multi-select-group">
                  <div className="multi-select-group-label">{group.label}</div>
                  {renderOptions(groupOpts)}
                </div>
              );
            })
          ) : (
            renderOptions(filtered ?? [])
          )}
          {selected.length > 0 && (
            <button type="button" className="multi-select-clear" onClick={() => onChange([])}>
              Clear
            </button>
          )}
        </div>
      )}
    </div>
  );
}
