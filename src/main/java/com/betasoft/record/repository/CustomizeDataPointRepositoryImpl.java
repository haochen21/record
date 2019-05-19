package com.betasoft.record.repository;

import com.betasoft.record.builder.AggregatorPoint;
import com.betasoft.record.model.DataPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.ReactiveCassandraOperations;
import reactor.core.publisher.Flux;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CustomizeDataPointRepositoryImpl implements CustomizeDataPointRepository {

    @Autowired
    private ReactiveCassandraOperations reactiveCassandraOperations;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomizeDataPointRepositoryImpl.class);

    @Override
    public Flux<AggregatorPoint> avg(String metric, String moc, List<String> mos, Date beginDate, Date endDate) {
        String moCondition = getMoCondition(mos);

        //cassandra driver保存日期数据时，自动转换成UTC时间，需要把日期转换成UTC时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder avgSql = new StringBuilder();
        avgSql.append("SELECT metric, moc, mo, avg(value) as value FROM data_point where metric ='").append(metric).append("' ");
        avgSql.append("and moc = '").append(moc).append("' ");
        avgSql.append("and mo").append(moCondition).append(" ");
        avgSql.append("and event_time >='").append(sdf.format(beginDate)).append("' ");
        avgSql.append("and event_time <='").append(sdf.format(endDate)).append("' ");
        avgSql.append("group by metric,mo");
        LOGGER.info(avgSql.toString());
        Flux<AggregatorPoint> aggregatorPoints = reactiveCassandraOperations.select(avgSql.toString(), AggregatorPoint.class);
        return aggregatorPoints;
    }

    @Override
    public Flux<AggregatorPoint> max(String metric, String moc, List<String> mos, Date beginDate, Date endDate) {
        String moCondition = getMoCondition(mos);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder maxSql = new StringBuilder();
        maxSql.append("SELECT metric, moc, mo, max(value) as value FROM data_point where metric ='").append(metric).append("' ");
        maxSql.append("and moc = '").append(moc).append("' ");
        maxSql.append("and mo").append(moCondition).append(" ");
        maxSql.append("and event_time >='").append(sdf.format(beginDate)).append("' ");
        maxSql.append("and event_time <='").append(sdf.format(endDate)).append("' ");
        maxSql.append("group by metric,mo");
        LOGGER.info(maxSql.toString());
        Flux<AggregatorPoint> aggregatorPoints = reactiveCassandraOperations.select(maxSql.toString(), AggregatorPoint.class);
        return aggregatorPoints;
    }

    @Override
    public Flux<AggregatorPoint> min(String metric, String moc, List<String> mos, Date beginDate, Date endDate) {
        String moCondition = getMoCondition(mos);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder minSql = new StringBuilder();
        minSql.append("SELECT metric, moc, mo, min(value) as value FROM data_point where metric ='").append(metric).append("' ");
        minSql.append("and moc = '").append(moc).append("' ");
        minSql.append("and mo").append(moCondition).append(" ");
        minSql.append("and event_time >='").append(sdf.format(beginDate)).append("' ");
        minSql.append("and event_time <='").append(sdf.format(endDate)).append("' ");
        minSql.append("group by metric,mo");
        LOGGER.info(minSql.toString());
        Flux<AggregatorPoint> aggregatorPoints = reactiveCassandraOperations.select(minSql.toString(), AggregatorPoint.class);
        return aggregatorPoints;
    }

    @Override
    public Flux<DataPoint> findSamplePoints(String metric, String moc, List<String> mos, Date beginDate, Date endDate) {
        String moCondition = getMoCondition(mos);
        //cassandra driver保存日期数据时，自动转换成UTC时间，需要把日期转换成UTC时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder findSql = new StringBuilder();
        findSql.append("SELECT * FROM data_point where metric ='").append(metric).append("' ");
        findSql.append("and moc = '").append(moc).append("' ");
        findSql.append("and mo").append(moCondition).append(" ");
        findSql.append("and event_time >='").append(sdf.format(beginDate)).append("' ");
        findSql.append("and event_time <='").append(sdf.format(endDate)).append("'");
        LOGGER.info(findSql.toString());
        Flux<DataPoint> dataPoints = reactiveCassandraOperations.select(findSql.toString(), DataPoint.class);
        return dataPoints;
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
}
