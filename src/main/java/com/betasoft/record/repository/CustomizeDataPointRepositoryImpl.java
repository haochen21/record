package com.betasoft.record.repository;

import com.betasoft.record.builder.AggregatorPoint;
import com.betasoft.record.model.DataPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.ReactiveCassandraOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.text.SimpleDateFormat;
import java.util.*;

public class CustomizeDataPointRepositoryImpl implements CustomizeDataPointRepository {

    @Autowired
    private ReactiveCassandraOperations reactiveCassandraOperations;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomizeDataPointRepositoryImpl.class);

    @Override
    public Flux<AggregatorPoint> avg(String metric, String moType, List<String> moIds, Date beginDate, Date endDate) {
        String moCondition = getMoCondition(moIds);

        //cassandra driver保存日期数据时，自动转换成UTC时间，需要把日期转换成UTC时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder avgSql = new StringBuilder();
        avgSql.append("SELECT metric, moc, mo, avg(value) as value FROM data_point where metric ='").append(metric).append("' ");
        avgSql.append("and moc = '").append(moType).append("' ");
        avgSql.append("and mo").append(moCondition).append(" ");
        avgSql.append("and event_time >='").append(sdf.format(beginDate)).append("' ");
        avgSql.append("and event_time <='").append(sdf.format(endDate)).append("' ");
        avgSql.append("group by metric,mo");
        LOGGER.info(avgSql.toString());
        Flux<AggregatorPoint> aggregatorPoints = reactiveCassandraOperations.select(avgSql.toString(), AggregatorPoint.class);
        return aggregatorPoints;
    }

    @Override
    public Flux<AggregatorPoint> max(String metric, String moType, List<String> moIds, Date beginDate, Date endDate) {
        String moCondition = getMoCondition(moIds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder maxSql = new StringBuilder();
        maxSql.append("SELECT metric, moc, mo, max(value) as value FROM data_point where metric ='").append(metric).append("' ");
        maxSql.append("and moc = '").append(moType).append("' ");
        maxSql.append("and mo").append(moCondition).append(" ");
        maxSql.append("and event_time >='").append(sdf.format(beginDate)).append("' ");
        maxSql.append("and event_time <='").append(sdf.format(endDate)).append("' ");
        maxSql.append("group by metric,mo");
        LOGGER.info(maxSql.toString());
        Flux<AggregatorPoint> aggregatorPoints = reactiveCassandraOperations.select(maxSql.toString(), AggregatorPoint.class);
        return aggregatorPoints;
    }

    @Override
    public Flux<AggregatorPoint> min(String metric, String moType, List<String> moIds, Date beginDate, Date endDate) {
        List<Date> queryDates = getDatesBetween(beginDate, endDate);
        String dayCondition = getDateCondition(queryDates);
        String moIdCondition = getMoCondition(moIds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder maxSql = new StringBuilder();
        maxSql.append("SELECT metric, mo_type as moType, mo_id as moId, day, min(value) as value FROM data_point where metric ='").append(metric).append("' ");
        maxSql.append("and mo_type = '").append(moType).append("' ");
        maxSql.append("and mo_id").append(moIdCondition).append(" ");
        maxSql.append("and day").append(dayCondition).append(" ");
        maxSql.append("and event_time >='").append(sdf.format(beginDate)).append("' ");
        maxSql.append("and event_time <='").append(sdf.format(endDate)).append("' ");
        maxSql.append("group by metric,mo_type,mo_id,day");
        LOGGER.info(maxSql.toString());
        Flux<AggregatorPoint> aggregatorPoints = reactiveCassandraOperations.select(maxSql.toString(), AggregatorPoint.class);
        return aggregatorPoints;

    }

    @Override
    public Flux<DataPoint> findSamplePoints(String metric, String moType, List<String> moIds, Date beginDate, Date endDate) {
        List<Date[]> queryDates = getQueryDates(beginDate, endDate);

        return Flux.fromIterable(queryDates)
                .flatMap(queryDate -> {
                    return Flux.fromIterable(moIds).map(moId -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                        SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        StringBuilder findSql = new StringBuilder();
                        findSql.append("SELECT * FROM data_point where metric ='").append(metric).append("' ");
                        findSql.append("and mo_type = '").append(moType).append("' ");
                        findSql.append("and mo_id = '").append(moId).append("'");
                        findSql.append("and day = '").append(daySdf.format(queryDate[0])).append("' ");
                        findSql.append("and event_time >='").append(sdf.format(queryDate[0])).append("' ");
                        findSql.append("and event_time <='").append(sdf.format(queryDate[1])).append("'");
                        return findSql.toString();
                    });
                })
                .log()
                .flatMap(findSql -> reactiveCassandraOperations.select(findSql, DataPoint.class))
                .log()
                .subscribeOn(Schedulers.newParallel("query"));

    }

    private String getMoCondition(List<String> mos) {
        StringBuilder moSb = new StringBuilder();
        if (mos.size() == 1) {
            moSb.append(" = '").append(mos.get(0)).append("'");
        } else {
            moSb.append(" in ('").append(mos.get(0)).append("'");
            for (int i = 1; i < mos.size(); i++) {
                moSb.append(",'").append(mos.get(i)).append("'");
            }
            moSb.append(")");
        }
        return moSb.toString();
    }

    private String getDateCondition(List<Date> dates) {
        SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder dateSb = new StringBuilder();
        if (dates.size() == 1) {
            dateSb.append(" = '").append(daySdf.format(dates.get(0))).append("'");
        } else {
            dateSb.append(" in ('").append(daySdf.format(dates.get(0))).append("'");
            for (int i = 1; i < dates.size(); i++) {
                dateSb.append(",'").append(daySdf.format(dates.get(i))).append("'");
            }
            dateSb.append(")");
        }
        return dateSb.toString();
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
