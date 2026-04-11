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

export type RankingQuery = {
  platformIds?: number[];
  genreIds?: number[];
  releaseYearMin?: number;
  releaseYearMax?: number;
  minPriceCents?: number;
  maxPriceCents?: number;
  minPlaytimeHours?: number;
  maxPlaytimeHours?: number;
  sort?: RankingSort;
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
};
