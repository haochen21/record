package com.betasoft.record.repository;

import com.betasoft.record.builder.AggregatorPoint;
import com.betasoft.record.builder.AggregatorPointWrapper;
import com.betasoft.record.model.DataPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.ReactiveCassandraOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.*;

public class CustomizeDataPointRepositoryImpl implements CustomizeDataPointRepository {

    @Autowired
    private ReactiveCassandraOperations reactiveCassandraOperations;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomizeDataPointRepositoryImpl.class);

    @Override
    public Mono<List<AggregatorPoint>> avg(String metric, List<String> tagJsons, Date beginDate, Date endDate) {
        List<Date[]> queryDates = getQueryDates(beginDate, endDate);

        return Flux.fromIterable(queryDates)
                .flatMap(queryDate -> Flux.fromIterable(tagJsons)
                        .map(tagJson -> {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                            SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            StringBuilder sql = new StringBuilder();
                            sql.append("SELECT metric, tag_json as tagJson, day, avg(value) as value FROM data_point where metric ='").append(metric).append("' ");
                            sql.append("and tag_json = '").append(tagJson).append("' ");
                            sql.append("and day = '").append(daySdf.format(queryDate[0])).append("' ");
                            sql.append("and event_time >='").append(sdf.format(queryDate[0])).append("' ");
                            sql.append("and event_time <='").append(sdf.format(queryDate[1])).append("'");
                            sql.append("group by metric,tag_json,day"); //不返回空值
                            return sql.toString();
                        }))
                //.log()
                .flatMap(sql -> reactiveCassandraOperations.select(sql, AggregatorPoint.class))
                //.log()
                .collectMultimap(ap -> ap.getMetric() + "." + ap.getTagJson())
                //.log()
                .map(agMap -> {
                    List<AggregatorPoint> minAgs = new ArrayList<>();
                    agMap.forEach((key, value) -> {
                        AggregatorPoint firstAg = value.iterator().next();
                        double avg = value.stream().mapToDouble(AggregatorPoint::getValue).average().getAsDouble();
                        firstAg.setValue(avg);
                        minAgs.add(firstAg);
                    });
                    return minAgs;
                });
    }

    @Override
    public Mono<List<AggregatorPoint>> max(String metric, List<String> tagJsons, Date beginDate, Date endDate) {
        List<Date[]> queryDates = getQueryDates(beginDate, endDate);

        return Flux.fromIterable(queryDates)
                .flatMap(queryDate -> Flux.fromIterable(tagJsons)
                        .map(tagJson -> {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                            SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            StringBuilder sql = new StringBuilder();
                            sql.append("SELECT metric, tag_json as tagJson, day, max(value) as value FROM data_point where metric ='").append(metric).append("' ");
                            sql.append("and tag_json = '").append(tagJson).append("' ");
                            sql.append("and day = '").append(daySdf.format(queryDate[0])).append("' ");
                            sql.append("and event_time >='").append(sdf.format(queryDate[0])).append("' ");
                            sql.append("and event_time <='").append(sdf.format(queryDate[1])).append("'");
                            sql.append("group by metric,tag_json,day"); //不返回空值
                            return sql.toString();
                        }))
                .flatMap(sql -> reactiveCassandraOperations.select(sql, AggregatorPointWrapper.class))
                .groupBy(apWrapper -> apWrapper.getAggregatorPointKey())
                .flatMap(groupFlux -> groupFlux.reduce(new AggregatorPoint(Double.MIN_VALUE), (a, b) -> {
                            AggregatorPoint maxAg = new AggregatorPoint();
                            maxAg.setMetric(groupFlux.key().getMetric());
                            maxAg.setTagJson(groupFlux.key().getTagJson());
                            if (a.getValue() > b.getValue()) {
                                maxAg.setValue(a.getValue());
                            } else {
                                maxAg.setValue(b.getValue());
                            }
                            return maxAg;
                        })
                ).collectList();
    }

    @Override
    public Mono<List<AggregatorPoint>> min(String metric, List<String> tagJsons, Date beginDate, Date endDate) {
        List<Date[]> queryDates = getQueryDates(beginDate, endDate);

        return Flux.fromIterable(queryDates)
                .flatMap(queryDate -> Flux.fromIterable(tagJsons)
                        .map(tagJson -> {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                            SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            StringBuilder sql = new StringBuilder();
                            sql.append("SELECT metric, tag_json as tagJson, day, min(value) as value FROM data_point where metric ='").append(metric).append("' ");
                            sql.append("and tag_json = '").append(tagJson).append("' ");
                            sql.append("and day = '").append(daySdf.format(queryDate[0])).append("' ");
                            sql.append("and event_time >='").append(sdf.format(queryDate[0])).append("' ");
                            sql.append("and event_time <='").append(sdf.format(queryDate[1])).append("'");
                            sql.append("group by metric,tag_json,day"); //不返回空值
                            return sql.toString();
                        }))
                .flatMap(sql -> reactiveCassandraOperations.select(sql, AggregatorPointWrapper.class))
                .groupBy(apWrapper -> apWrapper.getAggregatorPointKey())
                .flatMap(groupFlux -> groupFlux.reduce(new AggregatorPoint(Double.MAX_VALUE), (a, b) -> {
                            AggregatorPoint minAg = new AggregatorPoint();
                            minAg.setMetric(groupFlux.key().getMetric());
                            minAg.setTagJson(groupFlux.key().getTagJson());
                            if (a.getValue() <= b.getValue()) {
                                minAg.setValue(a.getValue());
                            } else {
                                minAg.setValue(b.getValue());
                            }
                            return minAg;
                        })
                ).collectList();
    }

    @Override
    public Flux<DataPoint> findSamplePoints(String metric, List<String> tagJsons, Date beginDate, Date endDate) {
        List<Date[]> queryDates = getQueryDates(beginDate, endDate);

        return Flux.fromIterable(queryDates)
                .flatMap(queryDate -> Flux.fromIterable(tagJsons)
                        .map(tagJson -> {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                            SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            StringBuilder findSql = new StringBuilder();
                            findSql.append("SELECT * FROM data_point where metric ='").append(metric).append("' ");
                            findSql.append("and tag_json = '").append(tagJson).append("' ");
                            findSql.append("and day = '").append(daySdf.format(queryDate[0])).append("' ");
                            findSql.append("and event_time >='").append(sdf.format(queryDate[0])).append("' ");
                            findSql.append("and event_time <='").append(sdf.format(queryDate[1])).append("'");
                            return findSql.toString();
                        }))
                .flatMap(findSql -> reactiveCassandraOperations.select(findSql, DataPoint.class));

    }

    private List<Date[]> getQueryDates(Date beginDate, Date endDate) {
        List<Date[]> queryDates = new ArrayList<>();

        List<Date> betweenDates = getDatesBetween(beginDate, endDate);
        for (int i = 0; i < betweenDates.size(); i++) {
            Calendar beginCalendar = new GregorianCalendar();
            if (i == 0) {
                beginCalendar.setTime(beginDate);
            } else {
                beginCalendar.setTime(betweenDates.get(i));
                beginCalendar.set(Calendar.HOUR_OF_DAY, 0);
                beginCalendar.set(Calendar.MINUTE, 0);
                beginCalendar.set(Calendar.SECOND, 0);
                beginCalendar.set(Calendar.MILLISECOND, 0);
            }

            Calendar endCalendar = new GregorianCalendar();
            if (i == betweenDates.size() - 1) {
                endCalendar.setTime(endDate);
            } else {
                endCalendar.setTime(betweenDates.get(i));
                endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                endCalendar.set(Calendar.MINUTE, 59);
                endCalendar.set(Calendar.SECOND, 59);
                endCalendar.set(Calendar.MILLISECOND, endCalendar.getActualMinimum(Calendar.MILLISECOND));
            }

            Date[] dates = {beginCalendar.getTime(), endCalendar.getTime()};
            queryDates.add(dates);
        }
        return queryDates;
    }

    private List<Date> getDatesBetween(Date startDate, Date endDate) {
        List<Date> datesInRange = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(endDate);
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);

        while (calendar.before(endCalendar)) {
            Date result = calendar.getTime();
            datesInRange.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        return datesInRange;
    }
}
