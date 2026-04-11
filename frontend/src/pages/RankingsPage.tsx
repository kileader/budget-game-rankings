import { useEffect, useReducer, useCallback, useState, useRef } from 'react';
import './RankingsPage.css';
import { getRankings } from '../api/rankings';
import { getPlatforms, getGenres } from '../api/metadata';
import { createConfig } from '../api/rankingConfigs';
import { ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';
import SavedConfigs from '../components/SavedConfigs';
import type { MetadataItem, RankingConfig, RankingPage, RankingQuery, RankingResult, RankingSort } from '../types';

const PAGE_LIMIT = 50;

const SORT_OPTIONS: { value: RankingSort; label: string }[] = [
  { value: 'VALUE_SCORE', label: 'Value Score' },
  { value: 'RATING', label: 'Rating' },
  { value: 'PLAYTIME', label: 'Playtime' },
  { value: 'PRICE', label: 'Price' },
  { value: 'TITLE', label: 'Title' },
  { value: 'RELEASE_DATE', label: 'Release Date' },
];

// --- State ---

type Filters = {
  platformIds: number[];
  genreIds: number[];
  releaseYearMin: string;
  releaseYearMax: string;
  minPriceDollars: string;
  maxPriceDollars: string;
  minPlaytimeHours: string;
  maxPlaytimeHours: string;
  sort: RankingSort;
};

type State = {
  filters: Filters;
  appliedQuery: RankingQuery;
  offset: number;
  data: RankingPage | null;
  loading: boolean;
  error: string | null;
};

type Action =
  | { type: 'SET_FILTER'; field: keyof Omit<Filters, 'platformIds' | 'genreIds'>; value: string }
  | { type: 'SET_MULTI_FILTER'; field: 'platformIds' | 'genreIds'; ids: number[] }
  | { type: 'APPLY_FILTERS' }
  | { type: 'LOAD_CONFIG'; config: RankingConfig }
  | { type: 'SET_OFFSET'; offset: number }
  | { type: 'FETCH_START' }
  | { type: 'FETCH_SUCCESS'; data: RankingPage }
  | { type: 'FETCH_ERROR'; error: string }
  | { type: 'SET_VALIDATION_ERROR'; error: string | null };

const defaultFilters: Filters = {
  platformIds: [],
  genreIds: [],
  releaseYearMin: '',
  releaseYearMax: '',
  minPriceDollars: '',
  maxPriceDollars: '',
  minPlaytimeHours: '',
  maxPlaytimeHours: '',
  sort: 'VALUE_SCORE',
};

function safeInt(s: string): number | undefined {
  const n = parseInt(s, 10);
  return isNaN(n) ? undefined : n;
}

function safeFloat(s: string): number | undefined {
  const n = parseFloat(s);
  return isNaN(n) ? undefined : n;
}

function filtersToQuery(filters: Filters, offset: number): RankingQuery {
  const q: RankingQuery = { sort: filters.sort, offset, limit: PAGE_LIMIT };
  if (filters.platformIds.length) q.platformIds = filters.platformIds;
  if (filters.genreIds.length) q.genreIds = filters.genreIds;
  if (filters.releaseYearMin) q.releaseYearMin = safeInt(filters.releaseYearMin);
  if (filters.releaseYearMax) q.releaseYearMax = safeInt(filters.releaseYearMax);
  const minPrice = safeFloat(filters.minPriceDollars);
  if (minPrice !== undefined) q.minPriceCents = Math.round(minPrice * 100);
  const maxPrice = safeFloat(filters.maxPriceDollars);
  if (maxPrice !== undefined) q.maxPriceCents = Math.round(maxPrice * 100);
  if (filters.minPlaytimeHours) q.minPlaytimeHours = safeFloat(filters.minPlaytimeHours);
  if (filters.maxPlaytimeHours) q.maxPlaytimeHours = safeFloat(filters.maxPlaytimeHours);
  return q;
}

function configToFilters(config: RankingConfig): Filters {
  return {
    platformIds: config.platformIds ?? [],
    genreIds: config.genreIds ?? [],
    releaseYearMin: config.releaseYearMin !== null ? String(config.releaseYearMin) : '',
    releaseYearMax: config.releaseYearMax !== null ? String(config.releaseYearMax) : '',
    minPriceDollars: config.minPriceCents !== null ? String(config.minPriceCents / 100) : '',
    maxPriceDollars: config.maxPriceCents !== null ? String(config.maxPriceCents / 100) : '',
    minPlaytimeHours: config.minPlaytimeHours !== null ? String(config.minPlaytimeHours) : '',
    maxPlaytimeHours: config.maxPlaytimeHours !== null ? String(config.maxPlaytimeHours) : '',
    sort: 'VALUE_SCORE',
  };
}

/** Block invalid ranges before the API; mirrors backend RankingService checks. */
function validateFilters(filters: Filters): string | null {
  const yrMin = filters.releaseYearMin === '' ? undefined : safeInt(filters.releaseYearMin);
  const yrMax = filters.releaseYearMax === '' ? undefined : safeInt(filters.releaseYearMax);
  if (yrMin !== undefined && yrMin < 0) {
    return 'Release year (from) cannot be negative.';
  }
  if (yrMax !== undefined && yrMax < 0) {
    return 'Release year (to) cannot be negative.';
  }
  if (yrMin !== undefined && yrMax !== undefined && yrMin > yrMax) {
    return 'Release year: “from” must be less than or equal to “to”.';
  }

  const minP = filters.minPriceDollars === '' ? undefined : safeFloat(filters.minPriceDollars);
  const maxP = filters.maxPriceDollars === '' ? undefined : safeFloat(filters.maxPriceDollars);
  if (minP !== undefined && minP < 0) {
    return 'Minimum price cannot be negative.';
  }
  if (maxP !== undefined && maxP < 0) {
    return 'Maximum price cannot be negative.';
  }
  if (minP !== undefined && maxP !== undefined && minP > maxP) {
    return 'Price: minimum must be less than or equal to maximum.';
  }

  const minH = filters.minPlaytimeHours === '' ? undefined : safeFloat(filters.minPlaytimeHours);
  const maxH = filters.maxPlaytimeHours === '' ? undefined : safeFloat(filters.maxPlaytimeHours);
  if (minH !== undefined && minH < 0) {
    return 'Minimum playtime cannot be negative.';
  }
  if (maxH !== undefined && maxH < 0) {
    return 'Maximum playtime cannot be negative.';
  }
  if (minH !== undefined && maxH !== undefined && minH > maxH) {
    return 'Playtime: minimum must be less than or equal to maximum.';
  }

  return null;
}

function filtersToConfigRequest(name: string, filters: Filters) {
  const req: {
    name: string;
    platformIds?: number[]; genreIds?: number[];
    releaseYearMin?: number; releaseYearMax?: number;
    minPriceCents?: number; maxPriceCents?: number;
    minPlaytimeHours?: number; maxPlaytimeHours?: number;
  } = { name };
  if (filters.platformIds.length) req.platformIds = filters.platformIds;
  if (filters.genreIds.length) req.genreIds = filters.genreIds;
  const yrMin = safeInt(filters.releaseYearMin);
  if (yrMin !== undefined) req.releaseYearMin = yrMin;
  const yrMax = safeInt(filters.releaseYearMax);
  if (yrMax !== undefined) req.releaseYearMax = yrMax;
  const minP = safeFloat(filters.minPriceDollars);
  if (minP !== undefined) req.minPriceCents = Math.round(minP * 100);
  const maxP = safeFloat(filters.maxPriceDollars);
  if (maxP !== undefined) req.maxPriceCents = Math.round(maxP * 100);
  const minH = safeFloat(filters.minPlaytimeHours);
  if (minH !== undefined) req.minPlaytimeHours = minH;
  const maxH = safeFloat(filters.maxPlaytimeHours);
  if (maxH !== undefined) req.maxPlaytimeHours = maxH;
  return req;
}

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'SET_FILTER':
      return {
        ...state,
        filters: { ...state.filters, [action.field]: action.value },
        error: null,
      };
    case 'SET_MULTI_FILTER':
      return {
        ...state,
        filters: { ...state.filters, [action.field]: action.ids },
        error: null,
      };
    case 'APPLY_FILTERS':
      return { ...state, offset: 0, appliedQuery: filtersToQuery(state.filters, 0) };
    case 'LOAD_CONFIG': {
      const filters = configToFilters(action.config);
      return { ...state, filters, offset: 0, appliedQuery: filtersToQuery(filters, 0) };
    }
    case 'SET_OFFSET':
      return { ...state, offset: action.offset, appliedQuery: filtersToQuery(state.filters, action.offset) };
    case 'FETCH_START':
      return { ...state, loading: true, error: null };
    case 'FETCH_SUCCESS':
      return { ...state, loading: false, data: action.data };
    case 'FETCH_ERROR':
      return { ...state, loading: false, error: action.error, data: null };
    case 'SET_VALIDATION_ERROR':
      return { ...state, error: action.error };
    default:
      return state;
  }
}

const initialState: State = {
  filters: defaultFilters,
  appliedQuery: { sort: 'VALUE_SCORE', offset: 0, limit: PAGE_LIMIT },
  offset: 0,
  data: null,
  loading: false,
  error: null,
};

// --- Helpers ---

function formatPrice(cents: number | null): string {
  if (cents === null) return '—';
  return `$${(cents / 100).toFixed(2)}`;
}

function formatNumber(n: number | null, decimals = 1): string {
  if (n === null) return '—';
  return n.toFixed(decimals);
}

// --- Sub-components ---

function MultiSelect({
  label,
  options,
  selected,
  onChange,
}: {
  label: string;
  options: MetadataItem[] | null;
  selected: number[];
  onChange: (ids: number[]) => void;
}) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  function toggle(id: number) {
    onChange(selected.includes(id) ? selected.filter(x => x !== id) : [...selected, id]);
  }

  const summary = selected.length === 0 ? 'Any' : `${selected.length} selected`;

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
          {options === null && <p className="multi-select-empty">Loading…</p>}
          {options !== null && options.length === 0 && <p className="multi-select-empty">None available</p>}
          {(options ?? []).map(opt => (
            <label key={opt.id} className="multi-select-option">
              <input
                type="checkbox"
                checked={selected.includes(opt.id)}
                onChange={() => toggle(opt.id)}
              />
              {opt.name}
            </label>
          ))}
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

function FilterBar({
  filters,
  platforms,
  genres,
  onFilterChange,
  onMultiFilterChange,
  onApply,
}: {
  filters: Filters;
  platforms: MetadataItem[] | null;
  genres: MetadataItem[] | null;
  onFilterChange: (field: keyof Omit<Filters, 'platformIds' | 'genreIds'>, value: string) => void;
  onMultiFilterChange: (field: 'platformIds' | 'genreIds', ids: number[]) => void;
  onApply: () => void;
}) {
  function field(label: string, key: keyof Omit<Filters, 'platformIds' | 'genreIds'>, placeholder: string, type = 'number') {
    return (
      <div className="filter-field">
        <label htmlFor={`filter-${key}`}>{label}</label>
        <input
          id={`filter-${key}`}
          type={type}
          placeholder={placeholder}
          value={filters[key] as string}
          onChange={e => onFilterChange(key, e.target.value)}
        />
      </div>
    );
  }

  return (
    <form
      className="filter-bar"
      onSubmit={e => { e.preventDefault(); onApply(); }}
      aria-label="Ranking filters"
    >
      <MultiSelect
        label="Platform"
        options={platforms}
        selected={filters.platformIds}
        onChange={ids => onMultiFilterChange('platformIds', ids)}
      />
      <MultiSelect
        label="Genre"
        options={genres}
        selected={filters.genreIds}
        onChange={ids => onMultiFilterChange('genreIds', ids)}
      />
      <div className="filter-group">
        <span className="filter-group-label">Release Year</span>
        {field('From', 'releaseYearMin', 'e.g. 2010')}
        {field('To', 'releaseYearMax', 'e.g. 2024')}
      </div>
      <div className="filter-group">
        <span className="filter-group-label">Price</span>
        {field('Min ($)', 'minPriceDollars', 'e.g. 5')}
        {field('Max ($)', 'maxPriceDollars', 'e.g. 60')}
      </div>
      <div className="filter-group">
        <span className="filter-group-label">Playtime (hrs)</span>
        {field('Min', 'minPlaytimeHours', 'e.g. 10')}
        {field('Max', 'maxPlaytimeHours', 'e.g. 100')}
      </div>
      <div className="filter-field">
        <label htmlFor="filter-sort">Sort by</label>
        <select
          id="filter-sort"
          value={filters.sort}
          onChange={e => onFilterChange('sort', e.target.value as RankingSort)}
        >
          {SORT_OPTIONS.map(o => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
      </div>
      <button type="submit">Apply</button>
    </form>
  );
}

function ResultRow({ result, rank }: { result: RankingResult; rank: number }) {
  return (
    <tr>
      <td>{rank}</td>
      <td>
        {result.coverImageUrl && (
          <img src={result.coverImageUrl} alt="" width={40} height={53} loading="lazy" />
        )}
      </td>
      <td>
        {result.igdbUrl ? (
          <a href={result.igdbUrl} target="_blank" rel="noreferrer">
            {result.title}
            <span className="sr-only"> (opens in new tab)</span>
          </a>
        ) : (
          result.title
        )}
      </td>
      <td>{formatNumber(result.igdbRating)}</td>
      <td>{result.hltbHours !== null ? `${formatNumber(result.hltbHours)} hrs` : '—'}</td>
      <td>
        {result.cheapsharkDealUrl ? (
          <a href={result.cheapsharkDealUrl} target="_blank" rel="noreferrer">
            {formatPrice(result.priceCents)}
          </a>
        ) : (
          formatPrice(result.priceCents)
        )}
      </td>
      <td>{formatNumber(result.valueScore, 2)}</td>
    </tr>
  );
}

function Pagination({
  offset,
  limit,
  total,
  onPage,
}: {
  offset: number;
  limit: number;
  total: number;
  onPage: (offset: number) => void;
}) {
  const page = Math.floor(offset / limit);
  const totalPages = Math.ceil(total / limit);
  if (totalPages <= 1) return null;

  return (
    <nav className="pagination" aria-label="Pagination">
      <button
        onClick={() => onPage(Math.max(0, offset - limit))}
        disabled={offset === 0}
        aria-label="Previous page"
      >
        Previous
      </button>
      <span>Page {page + 1} of {totalPages}</span>
      <button
        onClick={() => onPage(offset + limit)}
        disabled={offset + limit >= total}
        aria-label="Next page"
      >
        Next
      </button>
    </nav>
  );
}

// --- Page ---

export default function RankingsPage() {
  const { token, isLoggedIn } = useAuth();
  const [state, dispatch] = useReducer(reducer, initialState);
  const { filters, appliedQuery, offset, data, loading, error } = state;

  const [platforms, setPlatforms] = useState<MetadataItem[] | null>(null);
  const [genres, setGenres] = useState<MetadataItem[] | null>(null);

  useEffect(() => {
    void getPlatforms().then(setPlatforms).catch(() => setPlatforms([]));
    void getGenres().then(setGenres).catch(() => setGenres([]));
  }, []);

  const fetchData = useCallback(async () => {
    dispatch({ type: 'FETCH_START' });
    try {
      const result = await getRankings(appliedQuery);
      dispatch({ type: 'FETCH_SUCCESS', data: result });
    } catch (e) {
      const message =
        e instanceof ApiError ? e.message : 'Failed to load rankings.';
      dispatch({ type: 'FETCH_ERROR', error: message });
    }
  }, [appliedQuery]);

  useEffect(() => { void fetchData(); }, [fetchData]);

  async function handleSaveConfig(name: string) {
    if (!token) return;
    await createConfig(filtersToConfigRequest(name, filters), token);
  }

  return (
    <div className="rankings-page">
      <h1>Game Rankings</h1>
      <p className="rankings-subtitle">Ranked by value: rating × playtime ÷ price.</p>

      <section className="about-section" aria-label="How rankings work">
        <details>
          <summary>How does the value score work?</summary>
          <div className="about-body">
            <p>
              Each game is scored using: <strong>(IGDB rating × hours to beat) ÷ price</strong>.
              A game that costs $10, takes 50 hours, and scores 90 on IGDB ranks higher than a $60
              game that takes 8 hours and scores 95 — because you get more per dollar.
            </p>
            <p>
              Playtime comes from <a href="https://howlongtobeat.com" target="_blank" rel="noreferrer">HowLongToBeat</a>.
              If no playtime data exists, the genre average is used.
              Prices come from <a href="https://www.cheapshark.com" target="_blank" rel="noreferrer">CheapShark</a> (lowest current PC price).
              Ratings come from <a href="https://www.igdb.com" target="_blank" rel="noreferrer">IGDB</a>.
              Data refreshes nightly.
            </p>
            <p className="about-exclusions">
              Excluded: free/freemium games, multiplayer-only titles, games with fewer than 10 ratings, games with no price data.
            </p>
          </div>
        </details>
      </section>

      {isLoggedIn && token && (
        <SavedConfigs
          token={token}
          onLoad={config => dispatch({ type: 'LOAD_CONFIG', config })}
          onSave={handleSaveConfig}
        />
      )}

      <FilterBar
        filters={filters}
        platforms={platforms}
        genres={genres}
        onFilterChange={(field, value) => dispatch({ type: 'SET_FILTER', field, value })}
        onMultiFilterChange={(field, ids) => dispatch({ type: 'SET_MULTI_FILTER', field, ids })}
        onApply={() => {
          const err = validateFilters(filters);
          if (err) {
            dispatch({ type: 'SET_VALIDATION_ERROR', error: err });
            return;
          }
          dispatch({ type: 'SET_VALIDATION_ERROR', error: null });
          dispatch({ type: 'APPLY_FILTERS' });
        }}
      />

      {loading && <p className="status-message" role="status">Loading...</p>}
      {error && <p className="status-message error" role="alert">{error}</p>}

      {data && !loading && (
        <>
          <p className="result-count" aria-live="polite">{data.total} games</p>
          <div className="table-wrapper">
            <table className="rankings-table">
              <thead>
                <tr>
                  <th scope="col">#</th>
                  <th scope="col">Cover</th>
                  <th scope="col">Title</th>
                  <th scope="col">Rating</th>
                  <th scope="col">Playtime</th>
                  <th scope="col">Price</th>
                  <th scope="col">Value Score</th>
                </tr>
              </thead>
              <tbody>
                {data.results.map((result, i) => (
                  <ResultRow key={result.igdbGameId} result={result} rank={offset + i + 1} />
                ))}
              </tbody>
            </table>
          </div>
          <Pagination
            offset={offset}
            limit={PAGE_LIMIT}
            total={data.total}
            onPage={newOffset => dispatch({ type: 'SET_OFFSET', offset: newOffset })}
          />
        </>
      )}
    </div>
  );
}
