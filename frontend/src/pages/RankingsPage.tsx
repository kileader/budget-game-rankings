import { useEffect, useReducer, useCallback, useState, useRef, useId } from 'react';
import './RankingsPage.css';
import { getRankings } from '../api/rankings';
import { getPlatforms, getGenres } from '../api/metadata';
import { createConfig } from '../api/rankingConfigs';
import { ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useOnboarding } from '../context/OnboardingContext';
import SavedConfigs from '../components/SavedConfigs';
import MultiSelect from '../components/MultiSelect';
import type { MetadataItem, OnboardingPrefs, RankingConfig, RankingPage, RankingQuery, RankingResult, RankingSort, SortDirection } from '../types';

const PAGE_LIMIT = 50;

const RECENT_SEARCHES_KEY = 'bgr_search_recent';
const RECENT_SEARCHES_MAX = 8;

function loadRecentSearches(): string[] {
  try {
    const raw = localStorage.getItem(RECENT_SEARCHES_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as unknown;
    if (!Array.isArray(parsed)) return [];
    return parsed.filter((x): x is string => typeof x === 'string').slice(0, RECENT_SEARCHES_MAX);
  } catch {
    return [];
  }
}

function saveRecentSearches(entries: string[]) {
  try {
    localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(entries.slice(0, RECENT_SEARCHES_MAX)));
  } catch {
    /* ignore quota */
  }
}

function pushRecentSearch(title: string) {
  const t = title.trim();
  if (!t) return;
  const prev = loadRecentSearches().filter(s => s.toLowerCase() !== t.toLowerCase());
  saveRecentSearches([t, ...prev]);
}

const HIDDEN_GAMES_KEY = 'bgr_hidden_games';

function loadHiddenGameIds(): Set<number> {
  try {
    const raw = localStorage.getItem(HIDDEN_GAMES_KEY);
    if (!raw) return new Set();
    const parsed = JSON.parse(raw) as unknown;
    if (!Array.isArray(parsed)) return new Set();
    const ids = new Set<number>();
    for (const x of parsed) {
      if (typeof x === 'number' && Number.isInteger(x) && x > 0) ids.add(x);
    }
    return ids;
  } catch {
    return new Set();
  }
}

function saveHiddenGameIds(ids: Set<number>) {
  try {
    localStorage.setItem(HIDDEN_GAMES_KEY, JSON.stringify([...ids].sort((a, b) => a - b)));
  } catch {
    /* ignore quota */
  }
}

function hltbSearchUrl(title: string): string {
  return `https://howlongtobeat.com/?q=${encodeURIComponent(title.trim())}`;
}

function steamStoreUrl(steamAppId: number): string {
  return `https://store.steampowered.com/app/${steamAppId}/`;
}

/** Deal / store first, then IGDB (commerce-first cover tap). */
function primaryCoverHref(result: RankingResult): string | null {
  if (result.cheapsharkDealUrl) return result.cheapsharkDealUrl;
  if (result.steamAppId != null) return steamStoreUrl(result.steamAppId);
  if (result.igdbUrl) return result.igdbUrl;
  return null;
}

function primaryCoverAriaLabel(result: RankingResult): string {
  if (result.cheapsharkDealUrl) return `View deal for ${result.title} (opens in new tab)`;
  if (result.steamAppId != null) return `${result.title} on Steam (opens in new tab)`;
  if (result.igdbUrl) return `${result.title} on IGDB (opens in new tab)`;
  return '';
}

/** Order platforms like the filter catalog (API sort_order); unknown IDs last. */
function formatPlatformLabels(ids: number[] | undefined, catalog: MetadataItem[] | null): string {
  const list = ids?.length ? ids : [];
  if (list.length === 0) return '—';
  if (!catalog || catalog.length === 0) return list.map(String).join(', ');
  const order = new Map(catalog.map((p, i) => [p.id, i]));
  const sorted = [...list].sort((a, b) => (order.get(a) ?? 99999) - (order.get(b) ?? 99999));
  return sorted
    .map(id => catalog.find(p => p.id === id)?.name ?? `ID ${id}`)
    .join(', ');
}

/** Natural first-click direction per column. */
const SORT_DEFAULT_DIR: Record<RankingSort, SortDirection> = {
  VALUE_SCORE: 'DESC',
  RATING: 'DESC',
  PLAYTIME: 'DESC',
  PRICE: 'ASC',
  TITLE: 'ASC',
  RELEASE_DATE: 'DESC',
};

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
  title: string;
  ratingWeight: string;
  playtimeWeight: string;
  priceWeight: string;
  includeFreeToPlay: boolean;
  includeMultiplayerOnly: boolean;
  sort: RankingSort;
  sortDir: SortDirection;
};

type State = {
  filters: Filters;
  appliedQuery: RankingQuery;
  offset: number;
  data: RankingPage | null;
  loading: boolean;
  /** API / network failure (hides results). */
  error: string | null;
  /** Client-side filter validation (results may still show). */
  validationError: string | null;
};

type FilterField = keyof Omit<Filters, 'platformIds' | 'genreIds' | 'sort' | 'sortDir' | 'title' | 'ratingWeight' | 'playtimeWeight' | 'priceWeight' | 'includeFreeToPlay' | 'includeMultiplayerOnly'>;
type WeightField = 'ratingWeight' | 'playtimeWeight' | 'priceWeight';
type IncludeField = 'includeFreeToPlay' | 'includeMultiplayerOnly';

type Action =
  | { type: 'SET_FILTER'; field: FilterField; value: string }
  | { type: 'SET_TITLE'; value: string }
  | { type: 'SET_WEIGHT'; field: WeightField; value: string }
  | { type: 'SET_INCLUDE'; field: IncludeField; value: boolean }
  | { type: 'SET_MULTI_FILTER'; field: 'platformIds' | 'genreIds'; ids: number[] }
  | { type: 'SET_SORT'; sort: RankingSort; dir: SortDirection }
  | { type: 'APPLY_FILTERS' }
  | { type: 'LOAD_CONFIG'; config: RankingConfig }
  | { type: 'SET_OFFSET'; offset: number }
  | { type: 'FETCH_START' }
  | { type: 'FETCH_SUCCESS'; data: RankingPage }
  | { type: 'FETCH_ERROR'; error: string }
  | { type: 'SET_VALIDATION_ERROR'; error: string | null }
  | { type: 'APPLY_ONBOARDING'; prefs: OnboardingPrefs };

const defaultFilters: Filters = {
  platformIds: [],
  genreIds: [],
  releaseYearMin: '2000',
  releaseYearMax: '',
  minPriceDollars: '',
  maxPriceDollars: '',
  minPlaytimeHours: '',
  maxPlaytimeHours: '',
  title: '',
  ratingWeight: '1',
  playtimeWeight: '1',
  priceWeight: '1',
  includeFreeToPlay: false,
  includeMultiplayerOnly: false,
  sort: 'VALUE_SCORE',
  sortDir: 'DESC',
};

const YEAR_PRESET_MAP: Record<OnboardingPrefs['yearPreset'], string> = {
  modern: '2015',
  classic: '2005',
  all: '',
};

function filtersFromOnboarding(prefs: OnboardingPrefs | null): Filters {
  if (!prefs) return defaultFilters;
  return {
    ...defaultFilters,
    platformIds: prefs.platformIds,
    releaseYearMin: YEAR_PRESET_MAP[prefs.yearPreset],
    includeFreeToPlay: prefs.includeFreeToPlay,
    includeMultiplayerOnly: prefs.includeMultiplayerOnly,
  };
}

function safeInt(s: string): number | undefined {
  const n = parseInt(s, 10);
  return isNaN(n) ? undefined : n;
}

function safeFloat(s: string): number | undefined {
  const n = parseFloat(s);
  return isNaN(n) ? undefined : n;
}

function filtersToQuery(filters: Filters, offset: number): RankingQuery {
  const q: RankingQuery = { sort: filters.sort, sortDirection: filters.sortDir, offset, limit: PAGE_LIMIT };
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
  if (filters.title.trim()) q.title = filters.title.trim();
  const rW = safeFloat(filters.ratingWeight);
  if (rW !== undefined && rW !== 1) q.ratingWeight = rW;
  const pW = safeFloat(filters.playtimeWeight);
  if (pW !== undefined && pW !== 1) q.playtimeWeight = pW;
  const prW = safeFloat(filters.priceWeight);
  if (prW !== undefined && prW !== 1) q.priceWeight = prW;
  if (filters.includeFreeToPlay) q.includeFreeToPlay = true;
  if (filters.includeMultiplayerOnly) q.includeMultiplayerOnly = true;
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
    title: '',
    ratingWeight: String(config.ratingWeight ?? 1),
    playtimeWeight: String(config.playtimeWeight ?? 1),
    priceWeight: String(config.priceWeight ?? 1),
    includeFreeToPlay: false,
    includeMultiplayerOnly: false,
    sort: 'VALUE_SCORE',
    sortDir: 'DESC',
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
    ratingWeight?: number; playtimeWeight?: number; priceWeight?: number;
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
  const rW = safeFloat(filters.ratingWeight);
  if (rW !== undefined && rW !== 1) req.ratingWeight = rW;
  const pW = safeFloat(filters.playtimeWeight);
  if (pW !== undefined && pW !== 1) req.playtimeWeight = pW;
  const prW = safeFloat(filters.priceWeight);
  if (prW !== undefined && prW !== 1) req.priceWeight = prW;
  return req;
}

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'SET_FILTER':
      return {
        ...state,
        filters: { ...state.filters, [action.field]: action.value },
        error: null,
        validationError: null,
      };
    case 'SET_TITLE':
      return {
        ...state,
        filters: { ...state.filters, title: action.value },
        error: null,
        validationError: null,
      };
    case 'SET_WEIGHT':
      return {
        ...state,
        filters: { ...state.filters, [action.field]: action.value },
        error: null,
        validationError: null,
      };
    case 'SET_INCLUDE':
      return {
        ...state,
        filters: { ...state.filters, [action.field]: action.value },
        error: null,
        validationError: null,
      };
    case 'SET_MULTI_FILTER':
      return {
        ...state,
        filters: { ...state.filters, [action.field]: action.ids },
        error: null,
        validationError: null,
      };
    case 'SET_SORT': {
      const newFilters = { ...state.filters, sort: action.sort, sortDir: action.dir };
      return {
        ...state,
        filters: newFilters,
        offset: 0,
        appliedQuery: filtersToQuery(newFilters, 0),
        validationError: null,
      };
    }
    case 'APPLY_FILTERS':
      return { ...state, offset: 0, appliedQuery: filtersToQuery(state.filters, 0) };
    case 'LOAD_CONFIG': {
      const filters = configToFilters(action.config);
      return {
        ...state,
        filters,
        offset: 0,
        appliedQuery: filtersToQuery(filters, 0),
        validationError: null,
      };
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
      return { ...state, validationError: action.error };
    case 'APPLY_ONBOARDING': {
      const filters = filtersFromOnboarding(action.prefs);
      return {
        ...state,
        filters,
        offset: 0,
        appliedQuery: filtersToQuery(filters, 0),
        validationError: null,
      };
    }
    default:
      return state;
  }
}

function buildInitialState(prefs: OnboardingPrefs | null): State {
  const filters = filtersFromOnboarding(prefs);
  return {
    filters,
    appliedQuery: filtersToQuery(filters, 0),
    offset: 0,
    data: null,
    loading: false,
    error: null,
    validationError: null,
  };
}

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

function DualRangeSlider({
  rangeMin,
  rangeMax,
  step,
  valueMin,
  valueMax,
  onChangeMin,
  onChangeMax,
}: {
  rangeMin: number;
  rangeMax: number;
  step: number;
  valueMin: string;
  valueMax: string;
  onChangeMin: (v: string) => void;
  onChangeMax: (v: string) => void;
}) {
  const lo = valueMin === '' ? rangeMin : Math.max(rangeMin, Math.min(rangeMax, Number(valueMin)));
  const hi = valueMax === '' ? rangeMax : Math.max(rangeMin, Math.min(rangeMax, Number(valueMax)));
  const span = rangeMax - rangeMin;
  const loPercent = ((lo - rangeMin) / span) * 100;
  const hiPercent = ((hi - rangeMin) / span) * 100;

  return (
    <div className="dual-range" aria-hidden>
      <div className="dual-range-track">
        <div
          className="dual-range-fill"
          style={{ left: `${loPercent}%`, right: `${100 - hiPercent}%` }}
        />
      </div>
      <input
        type="range"
        className="dual-range-input"
        min={rangeMin}
        max={rangeMax}
        step={step}
        value={lo}
        style={{ zIndex: lo > rangeMin + span * 0.9 ? 5 : 3 }}
        onChange={e => {
          const v = Number(e.target.value);
          onChangeMin(v <= rangeMin ? '' : String(v));
        }}
      />
      <input
        type="range"
        className="dual-range-input"
        min={rangeMin}
        max={rangeMax}
        step={step}
        value={hi}
        style={{ zIndex: 4 }}
        onChange={e => {
          const v = Number(e.target.value);
          onChangeMax(v >= rangeMax ? '' : String(v));
        }}
      />
    </div>
  );
}

function FilterBar({
  filters,
  platforms,
  genres,
  onFilterChange,
  onMultiFilterChange,
  onTitleChange,
  onTitlePick,
  onTitleClear,
  onWeightChange,
  onIncludeChange,
  onApply,
}: {
  filters: Filters;
  platforms: MetadataItem[] | null;
  genres: MetadataItem[] | null;
  onFilterChange: (field: FilterField, value: string) => void;
  onMultiFilterChange: (field: 'platformIds' | 'genreIds', ids: number[]) => void;
  onTitleChange: (value: string) => void;
  onTitlePick: (value: string) => void;
  onTitleClear: () => void;
  onWeightChange: (field: WeightField, value: string) => void;
  onIncludeChange: (field: IncludeField, value: boolean) => void;
  onApply: () => void;
}) {
  const searchHintId = useId();
  const [recentOpen, setRecentOpen] = useState(false);
  const [recentList, setRecentList] = useState<string[]>(() => loadRecentSearches());
  const searchWrapRef = useRef<HTMLDivElement>(null);

  function field(label: string, key: FilterField, placeholder: string) {
    return (
      <div className="filter-field">
        <label htmlFor={`filter-${key}`}>{label}</label>
        <input
          id={`filter-${key}`}
          type="number"
          placeholder={placeholder}
          value={filters[key] as string}
          onChange={e => onFilterChange(key, e.target.value)}
        />
      </div>
    );
  }

  const currentYear = new Date().getFullYear();
  const q = filters.title.trim().toLowerCase();
  const suggestedRecent = recentList.filter(
    s => !q || s.toLowerCase().includes(q),
  ).slice(0, 8);

  useEffect(() => {
    function onDocMouseDown(e: MouseEvent) {
      if (!searchWrapRef.current?.contains(e.target as Node)) {
        setRecentOpen(false);
      }
    }
    document.addEventListener('mousedown', onDocMouseDown);
    return () => document.removeEventListener('mousedown', onDocMouseDown);
  }, []);

  useEffect(() => {
    function onStorage(e: StorageEvent) {
      if (e.key === RECENT_SEARCHES_KEY || e.key === null) {
        setRecentList(loadRecentSearches());
      }
    }
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  return (
    <form
      className="filter-bar"
      onSubmit={e => { e.preventDefault(); }}
      aria-label="Ranking filters"
    >
      <div className="filter-field filter-title-field" ref={searchWrapRef}>
        <label htmlFor="filter-title">Search</label>
        <div className="filter-title-row">
          <div className="filter-title-input-wrap">
            <input
              id="filter-title"
              type="text"
              autoComplete="off"
              aria-describedby={searchHintId}
              placeholder="Game title…"
              value={filters.title}
              onChange={e => onTitleChange(e.target.value)}
              onFocus={() => {
                setRecentList(loadRecentSearches());
                setRecentOpen(true);
              }}
              onBlur={() => {
                // allow mousedown on list option before close (handled via document listener)
              }}
            />
            {recentOpen && suggestedRecent.length > 0 && (
              <ul className="search-recent-list" aria-label="Recent searches">
                {suggestedRecent.map(s => (
                  <li key={s}>
                    <button
                      type="button"
                      className="search-recent-item"
                      onMouseDown={e => e.preventDefault()}
                      onClick={() => {
                        onTitlePick(s);
                        setRecentOpen(false);
                      }}
                    >
                      {s}
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
          {filters.title.length > 0 && (
            <button
              type="button"
              className="filter-title-clear"
              onClick={onTitleClear}
              aria-label="Clear search"
            >
              Clear
            </button>
          )}
        </div>
        <span id={searchHintId} className="filter-title-hint">
          Recent searches when focused; list narrows as you type. Applies after a short pause.
        </span>
      </div>
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
        <div className="filter-group-inputs filter-group-inputs-only">
          {field('From', 'releaseYearMin', '1980')}
          {field('To', 'releaseYearMax', String(currentYear))}
        </div>
      </div>
      <div className="filter-group filter-group-playtime">
        <span className="filter-group-label">Playtime (hrs)</span>
        <DualRangeSlider
          rangeMin={0} rangeMax={200} step={5}
          valueMin={filters.minPlaytimeHours} valueMax={filters.maxPlaytimeHours}
          onChangeMin={v => onFilterChange('minPlaytimeHours', v)}
          onChangeMax={v => onFilterChange('maxPlaytimeHours', v)}
        />
        <div className="filter-group-inputs filter-group-inputs-only">
          {field('Min', 'minPlaytimeHours', '0')}
          {field('Max', 'maxPlaytimeHours', '200')}
        </div>
      </div>
      <div className="filter-group">
        <span className="filter-group-label">Price ($)</span>
        <div className="filter-group-inputs filter-group-inputs-only">
          {field('Min', 'minPriceDollars', '0')}
          {field('Max', 'maxPriceDollars', '100')}
        </div>
      </div>
      <div className="filter-include-group">
        <label className="filter-checkbox">
          <input
            type="checkbox"
            checked={filters.includeFreeToPlay}
            onChange={e => onIncludeChange('includeFreeToPlay', e.target.checked)}
          />
          Include free-to-play
        </label>
        <label className="filter-checkbox">
          <input
            type="checkbox"
            checked={filters.includeMultiplayerOnly}
            onChange={e => onIncludeChange('includeMultiplayerOnly', e.target.checked)}
          />
          Include multiplayer-only
        </label>
      </div>
      <button type="button" className="filter-apply-btn" onClick={onApply}>Apply filters</button>
      <details className="scoring-advanced">
        <summary>Advanced Scoring</summary>
        <p className="scoring-advanced-hint">
          Weights are 0–2 (each raises rating, playtime, or price in the value formula). Click <strong>Apply filters</strong> after changing weights to refetch.
        </p>
        <div className="scoring-sliders">
          <label className="scoring-slider">
            <span>Rating weight: {filters.ratingWeight}</span>
            <input type="range" min="0" max="2" step="0.1" value={filters.ratingWeight} onChange={e => onWeightChange('ratingWeight', e.target.value)} />
          </label>
          <label className="scoring-slider">
            <span>Playtime weight: {filters.playtimeWeight}</span>
            <input type="range" min="0" max="2" step="0.1" value={filters.playtimeWeight} onChange={e => onWeightChange('playtimeWeight', e.target.value)} />
          </label>
          <label className="scoring-slider">
            <span>Price weight: {filters.priceWeight}</span>
            <input type="range" min="0" max="2" step="0.1" value={filters.priceWeight} onChange={e => onWeightChange('priceWeight', e.target.value)} />
          </label>
        </div>
      </details>
    </form>
  );
}

function SortableHeader({
  label,
  sortKey,
  currentSort,
  currentDir,
  onSort,
}: {
  label: string;
  sortKey: RankingSort;
  currentSort: RankingSort;
  currentDir: SortDirection;
  onSort: (sort: RankingSort, dir: SortDirection) => void;
}) {
  const isActive = currentSort === sortKey;
  const nextDir: SortDirection = isActive
    ? currentDir === 'DESC' ? 'ASC' : 'DESC'
    : SORT_DEFAULT_DIR[sortKey];

  return (
    <th scope="col">
      <button
        className={`sort-header${isActive ? ' sort-active' : ''}`}
        onClick={() => onSort(sortKey, nextDir)}
        aria-sort={isActive ? (currentDir === 'DESC' ? 'descending' : 'ascending') : 'none'}
      >
        {label}
        <span className="sort-indicator" aria-hidden>
          {isActive ? (currentDir === 'DESC' ? '▼' : '▲') : '⇅'}
        </span>
      </button>
    </th>
  );
}

type ViewMode = 'grid' | 'table';

function ResultRow({
  result,
  rank,
  onHide,
  platformsCatalog,
}: {
  result: RankingResult;
  rank: number;
  onHide: (igdbGameId: number) => void;
  platformsCatalog: MetadataItem[] | null;
}) {
  return (
    <tr>
      <td>{rank}</td>
      <td>
        {result.coverImageUrl &&
          (() => {
            const href = primaryCoverHref(result);
            return href ? (
              <a href={href} target="_blank" rel="noreferrer" aria-label={primaryCoverAriaLabel(result)}>
                <img src={result.coverImageUrl} alt="" width={40} height={53} loading="lazy" />
              </a>
            ) : (
              <img src={result.coverImageUrl} alt="" width={40} height={53} loading="lazy" />
            );
          })()}
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
      <td className="col-platforms">{formatPlatformLabels(result.platformIds, platformsCatalog)}</td>
      <td>{formatNumber(result.igdbRating)}</td>
      <td>
        {result.hltbHours !== null ? (
          <a
            className="table-external-link"
            href={hltbSearchUrl(result.title)}
            target="_blank"
            rel="noreferrer"
            title="HowLongToBeat (opens in new tab)"
          >
            {formatNumber(result.hltbHours)} hrs
            <span className="sr-only"> — HowLongToBeat, opens in new tab</span>
          </a>
        ) : (
          '—'
        )}
      </td>
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
      <td>
        <button
          type="button"
          className="dismiss-game-btn"
          onClick={() => onHide(result.igdbGameId)}
          aria-label={`Hide ${result.title} from this list`}
        >
          No thanks
        </button>
      </td>
    </tr>
  );
}

function GameCardSkeleton() {
  return (
    <article className="game-card game-card-skeleton" aria-hidden>
      <div className="game-card-cover skeleton-shimmer" />
      <div className="game-card-body">
        <div className="skeleton-line skeleton-title-block skeleton-shimmer" />
        <div className="skeleton-line skeleton-score-block skeleton-shimmer" />
        <div className="skeleton-line skeleton-label-block skeleton-shimmer" />
        <div className="skeleton-stats skeleton-shimmer" />
        <div className="skeleton-line skeleton-platforms skeleton-shimmer" />
      </div>
    </article>
  );
}

const GRID_SKELETON_COUNT = 16;
const TITLE_SEARCH_DEBOUNCE_MS = 300;

function RankingsTableSkeleton() {
  return (
    <div className="table-wrapper rankings-table-skeleton-wrap" aria-hidden>
      <table className="rankings-table rankings-table-skeleton">
        <thead>
          <tr>
            {Array.from({ length: 10 }, (_, i) => (
              <th key={i} scope="col"><span className="skeleton-line skeleton-th skeleton-shimmer" /></th>
            ))}
          </tr>
        </thead>
        <tbody>
          {Array.from({ length: 10 }, (_, r) => (
            <tr key={r}>
              {Array.from({ length: 10 }, (_, c) => (
                <td key={c}><span className="skeleton-line skeleton-td skeleton-shimmer" /></td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function GameCard({
  result,
  rank,
  onHide,
  platformsCatalog,
}: {
  result: RankingResult;
  rank: number;
  onHide: (igdbGameId: number) => void;
  platformsCatalog: MetadataItem[] | null;
}) {
  const coverInner = result.coverImageUrl ? (
    <img src={result.coverImageUrl} alt="" loading="lazy" />
  ) : (
    <div className="game-card-no-cover" />
  );
  const platformLine = formatPlatformLabels(result.platformIds, platformsCatalog);

  return (
    <article className="game-card">
      <div className="game-card-cover">
        <span className="game-card-rank">#{rank}</span>
        <button
          type="button"
          className="game-card-dismiss-x"
          onClick={e => {
            e.preventDefault();
            e.stopPropagation();
            onHide(result.igdbGameId);
          }}
          aria-label={`Hide ${result.title} from this list`}
        >
          <span aria-hidden>×</span>
        </button>
        {(() => {
          const href = primaryCoverHref(result);
          return href ? (
            <a
              href={href}
              target="_blank"
              rel="noreferrer"
              className="game-card-cover-link"
              aria-label={primaryCoverAriaLabel(result)}
            >
              {coverInner}
            </a>
          ) : (
            coverInner
          );
        })()}
      </div>
      <div className="game-card-body">
        <h3 className="game-card-title">
          {result.igdbUrl ? (
            <a href={result.igdbUrl} target="_blank" rel="noreferrer">{result.title}</a>
          ) : (
            result.title
          )}
        </h3>
        <div className="game-card-score">{formatNumber(result.valueScore, 2)}</div>
        <div className="game-card-label">Value Score</div>
        <div className="game-card-stats">
          <span title="IGDB Rating">⭐ {formatNumber(result.igdbRating)}</span>
          <span title="Price">
            {result.cheapsharkDealUrl ? (
              <a href={result.cheapsharkDealUrl} target="_blank" rel="noreferrer">
                {formatPrice(result.priceCents)}
              </a>
            ) : (
              formatPrice(result.priceCents)
            )}
          </span>
          <span title={result.hltbHours !== null ? 'HowLongToBeat (opens in new tab)' : 'Playtime'}>
            {result.hltbHours !== null ? (
              <a
                href={hltbSearchUrl(result.title)}
                target="_blank"
                rel="noreferrer"
                className="game-card-stat-link"
              >
                {formatNumber(result.hltbHours)}h
                <span className="sr-only"> — HowLongToBeat, opens in new tab</span>
              </a>
            ) : (
              '—'
            )}
          </span>
        </div>
        <p
          className="game-card-platforms"
          title={platformLine === '—' ? undefined : `Platforms (IGDB): ${platformLine}`}
        >
          {platformLine}
        </p>
      </div>
    </article>
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
  const { prefs } = useOnboarding();
  const [state, dispatch] = useReducer(reducer, prefs, buildInitialState);
  const { filters, appliedQuery, offset, data, loading, error, validationError } = state;

  const [viewMode, setViewMode] = useState<ViewMode>('grid');
  const [platforms, setPlatforms] = useState<MetadataItem[] | null>(null);
  const [genres, setGenres] = useState<MetadataItem[] | null>(null);
  const [hiddenIds, setHiddenIds] = useState(loadHiddenGameIds);

  useEffect(() => {
    void getPlatforms().then(setPlatforms).catch(() => setPlatforms([]));
    void getGenres().then(setGenres).catch(() => setGenres([]));
  }, []);

  useEffect(() => {
    const onStorage = (e: StorageEvent) => {
      if (e.key === HIDDEN_GAMES_KEY || e.key === null) setHiddenIds(loadHiddenGameIds());
    };
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  const hideGame = useCallback((igdbGameId: number) => {
    setHiddenIds(prev => {
      if (prev.has(igdbGameId)) return prev;
      const next = new Set(prev);
      next.add(igdbGameId);
      saveHiddenGameIds(next);
      return next;
    });
  }, []);

  const clearHiddenGames = useCallback(() => {
    setHiddenIds(new Set());
    saveHiddenGameIds(new Set());
  }, []);

  const prefsRef = useRef(prefs);
  useEffect(() => {
    if (prefs && prefs !== prefsRef.current) {
      dispatch({ type: 'APPLY_ONBOARDING', prefs });
    }
    prefsRef.current = prefs;
  }, [prefs]);

  const filtersRef = useRef(filters);
  filtersRef.current = filters;

  const appliedQueryRef = useRef(appliedQuery);
  appliedQueryRef.current = appliedQuery;

  const titleDebounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const skipInitialTitleDebounceRef = useRef(true);
  const ignoreNextTitleDebounceRef = useRef(false);

  const clearTitleDebounceTimer = useCallback(() => {
    if (titleDebounceTimerRef.current !== null) {
      clearTimeout(titleDebounceTimerRef.current);
      titleDebounceTimerRef.current = null;
    }
  }, []);

  useEffect(() => {
    const ac = new AbortController();
    dispatch({ type: 'FETCH_START' });
    void getRankings(appliedQuery, ac.signal)
      .then(result => {
        dispatch({ type: 'FETCH_SUCCESS', data: result });
      })
      .catch(e => {
        if (ac.signal.aborted) return;
        const message =
          e instanceof ApiError ? e.message : 'Failed to load rankings.';
        dispatch({ type: 'FETCH_ERROR', error: message });
      });
    return () => ac.abort();
  }, [appliedQuery]);

  useEffect(() => {
    if (skipInitialTitleDebounceRef.current) {
      skipInitialTitleDebounceRef.current = false;
      return;
    }
    if (ignoreNextTitleDebounceRef.current) {
      ignoreNextTitleDebounceRef.current = false;
      return;
    }
    clearTitleDebounceTimer();
    titleDebounceTimerRef.current = setTimeout(() => {
      titleDebounceTimerRef.current = null;
      const f = filtersRef.current;
      const err = validateFilters(f);
      if (err) {
        dispatch({ type: 'SET_VALIDATION_ERROR', error: err });
        const lastTitle = appliedQueryRef.current.title ?? '';
        if (f.title !== lastTitle) {
          ignoreNextTitleDebounceRef.current = true;
          dispatch({ type: 'SET_TITLE', value: lastTitle });
        }
        return;
      }
      dispatch({ type: 'SET_VALIDATION_ERROR', error: null });
      dispatch({ type: 'APPLY_FILTERS' });
      const t = f.title.trim();
      if (t) pushRecentSearch(t);
    }, TITLE_SEARCH_DEBOUNCE_MS);
    return () => {
      clearTitleDebounceTimer();
    };
  }, [filters.title, clearTitleDebounceTimer]);

  const handleTitlePick = useCallback(
    (s: string) => {
      clearTitleDebounceTimer();
      ignoreNextTitleDebounceRef.current = true;
      const f = { ...filtersRef.current, title: s };
      const err = validateFilters(f);
      if (err) {
        dispatch({ type: 'SET_VALIDATION_ERROR', error: err });
        dispatch({ type: 'SET_TITLE', value: appliedQueryRef.current.title ?? '' });
        return;
      }
      dispatch({ type: 'SET_VALIDATION_ERROR', error: null });
      dispatch({ type: 'SET_TITLE', value: s });
      dispatch({ type: 'APPLY_FILTERS' });
      pushRecentSearch(s);
    },
    [clearTitleDebounceTimer, dispatch],
  );

  const handleTitleClear = useCallback(() => {
    clearTitleDebounceTimer();
    ignoreNextTitleDebounceRef.current = true;
    const f = { ...filtersRef.current, title: '' };
    const err = validateFilters(f);
    if (err) {
      dispatch({ type: 'SET_VALIDATION_ERROR', error: err });
      dispatch({ type: 'SET_TITLE', value: appliedQueryRef.current.title ?? '' });
      return;
    }
    dispatch({ type: 'SET_VALIDATION_ERROR', error: null });
    dispatch({ type: 'SET_TITLE', value: '' });
    dispatch({ type: 'APPLY_FILTERS' });
  }, [clearTitleDebounceTimer, dispatch]);

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
        onTitleChange={value => dispatch({ type: 'SET_TITLE', value })}
        onTitlePick={handleTitlePick}
        onTitleClear={handleTitleClear}
        onWeightChange={(field, value) => dispatch({ type: 'SET_WEIGHT', field, value })}
        onIncludeChange={(field, value) => dispatch({ type: 'SET_INCLUDE', field, value })}
        onApply={() => {
          clearTitleDebounceTimer();
          const err = validateFilters(filters);
          if (err) {
            dispatch({ type: 'SET_VALIDATION_ERROR', error: err });
            return;
          }
          dispatch({ type: 'SET_VALIDATION_ERROR', error: null });
          dispatch({ type: 'APPLY_FILTERS' });
        }}
      />

      {error && <p className="status-message error" role="alert">{error}</p>}
      {validationError && (
        <p className="status-message error" role="alert">{validationError}</p>
      )}

      {(data || loading) && !error && (
        <>
          <div className="results-toolbar">
            <div className="result-count-stack">
              <p className="result-count" aria-live="polite">
                {data ? (
                  <>
                    {data.total} games
                    {' · '}
                    <span className="hidden-count">{hiddenIds.size} hidden</span>
                    <button
                      type="button"
                      className="clear-hidden-btn"
                      disabled={hiddenIds.size === 0}
                      onClick={clearHiddenGames}
                    >
                      Show hidden
                    </button>
                  </>
                ) : (
                  '…'
                )}
              </p>
              <div className="result-count-subrow" aria-live="polite">
                {!loading && data && (() => {
                  const hiddenHere = data.results.filter(r => hiddenIds.has(r.igdbGameId)).length;
                  const shownHere = data.results.length - hiddenHere;
                  if (hiddenHere === 0 || shownHere === 0) return null;
                  return (
                    <span className="hidden-page-hint">
                      {hiddenHere} hidden on this page ({shownHere} shown)
                    </span>
                  );
                })()}
              </div>
            </div>
            <div className="view-toggle" role="radiogroup" aria-label="View mode">
              <button
                className={viewMode === 'grid' ? 'active' : ''}
                onClick={() => setViewMode('grid')}
                aria-pressed={viewMode === 'grid'}
                title="Grid view"
              >▦</button>
              <button
                className={viewMode === 'table' ? 'active' : ''}
                onClick={() => setViewMode('table')}
                aria-pressed={viewMode === 'table'}
                title="Table view"
              >☰</button>
            </div>
          </div>

          {loading && (
            viewMode === 'grid' ? (
              <div className="game-grid">
                {Array.from({ length: GRID_SKELETON_COUNT }, (_, i) => (
                  <GameCardSkeleton key={i} />
                ))}
              </div>
            ) : (
              <RankingsTableSkeleton />
            )
          )}

          {!loading && data && (() => {
            const visibleRows = data.results
              .map((result, i) => ({ result, rank: offset + i + 1 }))
              .filter(({ result }) => !hiddenIds.has(result.igdbGameId));
            const hiddenOnPage = data.results.length - visibleRows.length;

            return (
              <>
                {visibleRows.length === 0 && data.results.length > 0 && (
                  <p className="status-message" role="status">
                    Every game on this page is hidden ({hiddenOnPage}).
                    {' '}
                    <button type="button" className="clear-hidden-btn" onClick={clearHiddenGames}>
                      Show all hidden games
                    </button>
                    {' '}
                    or use the next page.
                  </p>
                )}
                {viewMode === 'grid' ? (
                  <div className="game-grid">
                    {visibleRows.map(({ result, rank }) => (
                      <GameCard
                        key={result.igdbGameId}
                        result={result}
                        rank={rank}
                        onHide={hideGame}
                        platformsCatalog={platforms}
                      />
                    ))}
                  </div>
                ) : (
                  <div className="table-wrapper">
                    <table className="rankings-table">
                      <thead>
                        <tr>
                          <th scope="col">#</th>
                          <th scope="col">Cover</th>
                          <SortableHeader label="Title" sortKey="TITLE" currentSort={filters.sort} currentDir={filters.sortDir} onSort={(s, d) => dispatch({ type: 'SET_SORT', sort: s, dir: d })} />
                          <th scope="col" className="col-platforms">Platforms</th>
                          <SortableHeader label="Rating" sortKey="RATING" currentSort={filters.sort} currentDir={filters.sortDir} onSort={(s, d) => dispatch({ type: 'SET_SORT', sort: s, dir: d })} />
                          <SortableHeader label="Playtime" sortKey="PLAYTIME" currentSort={filters.sort} currentDir={filters.sortDir} onSort={(s, d) => dispatch({ type: 'SET_SORT', sort: s, dir: d })} />
                          <SortableHeader label="Price" sortKey="PRICE" currentSort={filters.sort} currentDir={filters.sortDir} onSort={(s, d) => dispatch({ type: 'SET_SORT', sort: s, dir: d })} />
                          <SortableHeader label="Value Score" sortKey="VALUE_SCORE" currentSort={filters.sort} currentDir={filters.sortDir} onSort={(s, d) => dispatch({ type: 'SET_SORT', sort: s, dir: d })} />
                          <th scope="col" className="col-actions"><span className="sr-only">Hide</span></th>
                        </tr>
                      </thead>
                      <tbody>
                        {visibleRows.map(({ result, rank }) => (
                          <ResultRow
                            key={result.igdbGameId}
                            result={result}
                            rank={rank}
                            onHide={hideGame}
                            platformsCatalog={platforms}
                          />
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </>
            );
          })()}

          {!loading && data && (
            <Pagination
              offset={offset}
              limit={PAGE_LIMIT}
              total={data.total}
              onPage={newOffset => dispatch({ type: 'SET_OFFSET', offset: newOffset })}
            />
          )}
        </>
      )}
    </div>
  );
}
