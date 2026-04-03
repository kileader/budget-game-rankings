// --- Game / Ranking ---

export type Game = {
  id: number;
  title: string;
  igdbRating: number;
  hoursToBeat: number | null;
  price: number | null;
  valueScore: number | null;
  platforms: string[];
  genres: string[];
  releaseYear: number | null;
  isFree: boolean;
  isMultiplayerOnly: boolean;
};

export type RankedGame = Game & {
  rank: number;
};

export type RankingFilters = {
  platform?: string;
  genre?: string;
  minPrice?: number;
  maxPrice?: number;
  minHours?: number;
  maxHours?: number;
  releaseYearFrom?: number;
  releaseYearTo?: number;
  page?: number;
  size?: number;
  sort?: string;
};

export type PagedResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
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

export type RankingConfig = {
  id: number;
  name: string;
  filters: RankingFilters;
  createdAt: string;
  updatedAt: string;
};

export type RankingConfigRequest = {
  name: string;
  filters: RankingFilters;
};
