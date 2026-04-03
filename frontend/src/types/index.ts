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
};

export type UserProfile = {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  active: boolean;
};

// --- Ranking Configs ---
// Note: RankingConfig.filters shape needs verification against backend RankingConfigDto before Phase 9.

export type RankingConfig = {
  id: number;
  name: string;
  filters: RankingQuery;
  createdAt: string;
  updatedAt: string;
};

export type RankingConfigRequest = {
  name: string;
  filters: RankingQuery;
};
