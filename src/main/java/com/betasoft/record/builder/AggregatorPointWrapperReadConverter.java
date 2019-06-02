package com.betasoft.record.builder;

import com.datastax.driver.core.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class AggregatorPointWrapperReadConverter implements Converter<Row, AggregatorPointWrapper> {
    @Override
    public AggregatorPointWrapper convert(Row row) {
        String metric = row.getString("metric");
        String tagJson = row.getString("tagJson");
        double value = row.getDouble("value");

        return new AggregatorPointWrapper(new AggregatorPointKey(metric, tagJson), value);
    }
}
