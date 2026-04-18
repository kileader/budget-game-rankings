import { api } from './client';
import type { RankingPage, RankingQuery } from '../types';

export function getRankings(query: RankingQuery = {}, signal?: AbortSignal): Promise<RankingPage> {
  const params = new URLSearchParams();

  if (query.platformIds?.length) query.platformIds.forEach(id => params.append('platformIds', String(id)));
  if (query.genreIds?.length) query.genreIds.forEach(id => params.append('genreIds', String(id)));
  if (query.releaseYearMin !== undefined) params.set('releaseYearMin', String(query.releaseYearMin));
  if (query.releaseYearMax !== undefined) params.set('releaseYearMax', String(query.releaseYearMax));
  if (query.minPriceCents !== undefined) params.set('minPriceCents', String(query.minPriceCents));
  if (query.maxPriceCents !== undefined) params.set('maxPriceCents', String(query.maxPriceCents));
  if (query.minPlaytimeHours !== undefined) params.set('minPlaytimeHours', String(query.minPlaytimeHours));
  if (query.maxPlaytimeHours !== undefined) params.set('maxPlaytimeHours', String(query.maxPlaytimeHours));
  if (query.title) params.set('title', query.title);
  if (query.ratingWeight !== undefined && query.ratingWeight !== 1) params.set('ratingWeight', String(query.ratingWeight));
  if (query.playtimeWeight !== undefined && query.playtimeWeight !== 1) params.set('playtimeWeight', String(query.playtimeWeight));
  if (query.priceWeight !== undefined && query.priceWeight !== 1) params.set('priceWeight', String(query.priceWeight));
  if (query.includeFreeToPlay) params.set('includeFreeToPlay', 'true');
  if (query.includeMultiplayerOnly) params.set('includeMultiplayerOnly', 'true');
  if (query.excludeAdultRated) params.set('excludeAdultRated', 'true');
  if (query.sort) params.set('sort', query.sort);
  if (query.sortDirection) params.set('sortDirection', query.sortDirection);
  if (query.offset !== undefined) params.set('offset', String(query.offset));
  if (query.limit !== undefined) params.set('limit', String(query.limit));

  const qs = params.toString();
  return api.get<RankingPage>(`/rankings${qs ? `?${qs}` : ''}`, { signal });
}
