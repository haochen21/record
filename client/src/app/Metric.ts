import { Aggregator } from './Aggregator';
import { GroupBy } from './GroupBy';

export class Metric {

    name: string;

    tags: object = {};

    aggregators: Aggregator[];

    group_by: GroupBy;
}