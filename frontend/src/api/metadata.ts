import { api } from './client';
import type { MetadataItem } from '../types';

export function getPlatforms(): Promise<MetadataItem[]> {
  return api.get<MetadataItem[]>('/metadata/platforms');
}

export function getGenres(): Promise<MetadataItem[]> {
  return api.get<MetadataItem[]>('/metadata/genres');
}
