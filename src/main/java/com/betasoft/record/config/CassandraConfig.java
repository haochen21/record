package com.betasoft.record.config;

import com.betasoft.record.repository.ExtendedReactiveCassandraRepositoryImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableReactiveCassandraRepositories(basePackages = {"com.betasoft.record.repository"}, repositoryBaseClass = ExtendedReactiveCassandraRepositoryImpl.class)
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${cassandra.contact-points}")
    private String contactPoints;

    @Value("${cassandra.port}")
    private int port;

    @Value("${cassandra.keyspace-name}")
    private String keyspace;

    @Override
    protected List<String> getStartupScripts() {
        String keyspace = "CREATE KEYSPACE IF NOT EXISTS " + getKeyspaceName()
                + " WITH durable_writes = true"
                + " AND replication = { 'replication_factor' : 1, 'class' : 'SimpleStrategy' };";

        String dataPointTable = "" +
                "CREATE TABLE IF NOT EXISTS " + getKeyspaceName() + ".data_point (\n" +
                "  metric text,\n" +
                "  mo text,\n" +
                "  day text,\n" +
                "  event_time timestamp,\n" +
                "  value double,\n" +
                "  tags map<text, text>,\n" +
                "  PRIMARY KEY ((metric, mo, day), event_time)\n" +
                ")\n" +
                " WITH gc_grace_seconds = 60\n" +
                " AND default_time_to_live = 1800000\n" +
                " AND memtable_flush_period_in_ms = 600\n" +
                " AND compaction = {'compaction_window_size': '14'," +
                " 'compaction_window_unit': 'DAYS'," +
                " 'class': 'org.apache.cassandra.db.compaction.TimeWindowCompactionStrategy'" +
                "};";

        String metricTable = "" +
                "CREATE TABLE IF NOT EXISTS " + getKeyspaceName() + ".mo (\n" +
                "  metric text,\n" +
                "  category text,\n" +
                "  mo_path text,\n" +
                "  tags map<text, text>,\n" +
                "  PRIMARY KEY (metric,category)\n" +
                ")\n" +
                " WITH gc_grace_seconds = 60\n" +
                " AND default_time_to_live = 1800000\n" +
                " AND memtable_flush_period_in_ms = 600\n" +
                " AND compaction = {'compaction_window_size': '14'," +
                " 'compaction_window_unit': 'DAYS'," +
                " 'class': 'org.apache.cassandra.db.compaction.TimeWindowCompactionStrategy'" +
                "};";

        String moTagsInx = "CREATE INDEX IF NOT EXISTS mo_tags_idx ON " + getKeyspaceName() + ".mo (ENTRIES(tags))";

        return Arrays.asList(keyspace, dataPointTable, metricTable, moTagsInx);
    }

    @Override
    protected String getKeyspaceName() {
        return keyspace;
    }

    @Override
    protected String getContactPoints() {
        return this.contactPoints;
    }

    @Override
    protected int getPort() {
        return this.port;
    }

    @Override
    protected boolean getMetricsEnabled() {
        return false;
    }
}
