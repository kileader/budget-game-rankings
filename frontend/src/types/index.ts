// --- Metadata ---

export type MetadataItem = {
  id: number;
  name: string;
};

// --- Ranking ---

export type RankingResult = {
  igdbGameId: number;
  title: string;
  igdbRating: number;
  hltbHours: number | null;
  priceCents: number | null;
  valueScore: number | null;
  coverImageUrl: string | null;
  igdbUrl: string | null;
  cheapsharkDealUrl: string | null;
  /** Present when backend has a Steam listing; omitted on older API responses. */
  steamAppId?: number | null;
  /** IGDB platform IDs; resolve names via `/metadata/platforms`. */
  platformIds?: number[];
  /** E.g. "ESRB · Teen" from IGDB age ratings; absent when unknown. */
  ageRatingDisplay?: string | null;
};

export type RankingPage = {
  offset: number;
  limit: number;
  total: number;
  results: RankingResult[];
};

export type RankingSort =
  | 'VALUE_SCORE'
  | 'RATING'
  | 'PLAYTIME'
  | 'PRICE'
  | 'TITLE'
  | 'RELEASE_DATE';

export type SortDirection = 'ASC' | 'DESC';

export type RankingQuery = {
  platformIds?: number[];
  genreIds?: number[];
  releaseYearMin?: number;
  releaseYearMax?: number;
  minPriceCents?: number;
  maxPriceCents?: number;
  minPlaytimeHours?: number;
  maxPlaytimeHours?: number;
  title?: string;
  ratingWeight?: number;
  playtimeWeight?: number;
  priceWeight?: number;
  includeFreeToPlay?: boolean;
  includeMultiplayerOnly?: boolean;
  /** Omit games with Mature / 18+ style IGDB content labels; unrated games still included. */
  excludeAdultRated?: boolean;
  sort?: RankingSort;
  sortDirection?: SortDirection;
  offset?: number;
  limit?: number;
};

// --- Auth ---

export type LoginRequest = {
  username: string;
  password: string;
};

export type SignupRequest = {
  username: string;
  email: string;
  password: string;
};

export type AuthResponse = {
  token: string;
  username: string;
  role: string;
};

export type CurrentUser = {
  id: number;
  username: string;
  email: string;
  role: string;
};

// --- Onboarding ---

export type OnboardingPrefs = {
  platformIds: number[];
  yearPreset: 'modern' | 'classic' | 'all';
  includeFreeToPlay: boolean;
  includeMultiplayerOnly: boolean;
};

// --- Ranking Configs ---
// Flat shape — mirrors backend RankingConfigDto directly (no nested filters object).

export type RankingConfig = {
  id: number;
  name: string;
  platformIds: number[] | null;
  genreIds: number[] | null;
  releaseYearMin: number | null;
  releaseYearMax: number | null;
  minPriceCents: number | null;
  maxPriceCents: number | null;
  minPlaytimeHours: number | null;
  maxPlaytimeHours: number | null;
  ratingWeight: number;
  playtimeWeight: number;
  priceWeight: number;
  /** Omitted on older API responses; treat as false. */
  excludeAdultRated?: boolean;
  createdAt: string;
  updatedAt: string;
};

export type RankingConfigRequest = {
  name: string;
  platformIds?: number[];
  genreIds?: number[];
  releaseYearMin?: number;
  releaseYearMax?: number;
  minPriceCents?: number;
  maxPriceCents?: number;
  minPlaytimeHours?: number;
  maxPlaytimeHours?: number;
  ratingWeight?: number;
  playtimeWeight?: number;
  priceWeight?: number;
  excludeAdultRated?: boolean;
};
