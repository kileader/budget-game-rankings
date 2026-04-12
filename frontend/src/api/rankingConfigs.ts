import { api } from './client';
import type { RankingConfig, RankingConfigRequest } from '../types';

export function listConfigs(token: string): Promise<RankingConfig[]> {
  return api.get<RankingConfig[]>('/users/me/ranking-configs', token);
}

export function createConfig(body: RankingConfigRequest, token: string): Promise<RankingConfig> {
  return api.post<RankingConfig>('/users/me/ranking-configs', body, token);
}

export function updateConfig(id: number, body: RankingConfigRequest, token: string): Promise<RankingConfig> {
  return api.put<RankingConfig>(`/users/me/ranking-configs/${id}`, body, token);
}

export function deleteConfig(id: number, token: string): Promise<void> {
  return api.delete<void>(`/users/me/ranking-configs/${id}`, token);
}
