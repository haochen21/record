import { Component, ViewEncapsulation } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { HttpClient } from "@angular/common/http";

import { en_US, zh_CN, NzI18nService } from 'ng-zorro-antd';

import { QueryBuilder } from './QueryBuilder';
import { Metric } from './Metric';
import { Aggregator } from './Aggregator';
import { GroupBy } from './GroupBy';
import { Tag } from './Tag';
import { TagValue } from './TagValue';

declare var require: any;
const Highcharts = require('highcharts');

@Component({
  selector: 'app-root',
  encapsulation: ViewEncapsulation.None,
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  beginDate: Date = new Date();

  beginTime: Date = new Date(0, 0, 0, 0, 0, 0);

  endDate: Date = new Date();

  endTime: Date = new Date(0, 0, 0, 23, 59, 59);

  metricValue: string;

  metrics = [];

  tagKeys = [];

  tagValues: { [tagKey: string]: string[] } = { };
  
  tags: Tag[] = [];

  selectedAggregatorValue = '';

  queryBuilder: QueryBuilder = new QueryBuilder();

  options: Object;

  constructor(private i18n: NzI18nService,
    private http: HttpClient) {
    this.i18n.setLocale(zh_CN);
    Highcharts.setOptions({ global: { useUTC: false } });

    this.tags.push(new Tag("", ""));
  }

  onMetricSerach(value: string): void {
    let url = "/api/v1/metric";
    let body = {};
    this.tagValues = {};
    this.tags = [];
    body["metric"] = value;
    this.http
      .post(url, body, {})
      .subscribe((data: any) => {
        this.metrics = data;
      });
  }

  metricChange(value: string): void {
    this.tagKeys = [];
    let url = "/api/v1/metric/tagKey";
    let body = {};
    body["metric"] = value;
    this.http
      .post(url, body, {})
      .subscribe((data: any) => {
        this.tagKeys = data;
      });
  }

  tagKeyChange(index: number, value: string): void {    
    this.tags[index] = new Tag(value, "");
    let url = "/api/v1/metric/tagValue";
    let body = {};
    body["metric"] = this.metricValue;
    body["tagKey"] = value;
    this.http
      .post(url, body, {})
      .subscribe((data: any) => {
        this.tagValues[value] = data;
      });
  }

  addTag(e?: MouseEvent): void {
    if (e) {
      e.preventDefault();
    }
    this.tags.push(new Tag("", ""));
  }

  deleteTag(index: number, e: MouseEvent): void {
    if (e) {
      e.preventDefault();
    }
    this.tags.splice(index, 1);
  }

  title = 'client';

  onSearch(): void {
    let searchBeginDate: Date = this.beginDate;
    searchBeginDate.setHours(this.beginTime.getHours());
    searchBeginDate.setMinutes(this.beginTime.getMinutes());
    searchBeginDate.setSeconds(this.beginTime.getSeconds());
    searchBeginDate.setMilliseconds(0);
    this.queryBuilder.start_absolute = searchBeginDate.getTime();

    let searchEndDate: Date = this.endDate;
    searchEndDate.setHours(this.endTime.getHours());
    searchEndDate.setMinutes(this.endTime.getMinutes());
    searchEndDate.setSeconds(this.endTime.getSeconds());
    searchEndDate.setMilliseconds(999);
    this.queryBuilder.end_absolute = searchEndDate.getTime();

    let metric: Metric = new Metric();
    metric.name = this.metricValue;

    this.tags.forEach(function (tag) {
      if (tag.key !== '' && tag.value !== '') {
        if (!metric.tags[tag.key]) {
          metric.tags[tag.key] = [tag.value];
        } else {
          metric.tags[tag.key].push(tag.value);
        }
      }
    });

    if (this.selectedAggregatorValue) {
      let aggregator: Aggregator = new Aggregator();
      aggregator.name = this.selectedAggregatorValue;
      metric.aggregators = [aggregator];
    }

    let groupBy: GroupBy = new GroupBy();
    metric.group_by = groupBy;

    this.queryBuilder.metrics = [metric];

    this.http
      .post("/api/v1/datapoints/query", this.queryBuilder, { observe: 'response' })
      .subscribe(response => {
        console.log(response.headers.get('executetime'));
        let series = [];

        let results = response.body["queries"][0].results;
        for (let i = 0; i < results.length; i++) {
          let serie: object = {};

          let result = results[i];

          serie["name"] = JSON.stringify(result.tags);;
          serie["data"] = [];
          let values = result.values;
          values.forEach(element => {
            serie["data"].push([element[0], element[1]])
          });
          series.push(serie);
        }

        this.options = {
          chart: {
            type: 'spline'
          },
          title: { text: '执行时间：' + response.headers.get('executetime') + ' ms' },
          xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: {
              day: '%m-%d'
            },
            labels: {
              overflow: 'justify'
            }
          },
          tooltip: {
            headerFormat: '<b>{series.name}</b><br>',
            pointFormat: '{point.x:%Y-%m-%d %H:%M:%S}: {point.y:.2f}'
          },
          series: series
        };
      });

  }
}
