import { Metric } from './Metric';

export class QueryBuilder {

    start_absolute: number;

    end_absolute: number;

    cache_time: number = 0;

    metrics: Metric[];
}