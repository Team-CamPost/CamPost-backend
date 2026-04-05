package com.campost.backend.features.crawl.repository;

import com.campost.backend.features.crawl.dto.CrawlJobDto;
import com.campost.backend.features.crawl.dto.ParseLogDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CrawlQueryRepository {

    private final JdbcClient jdbcClient;

    public CrawlQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<CrawlJobDto> findRecentCrawlJobs(int limit) {
        String sql = """
                SELECT id, source_id, started_at, finished_at, status,
                       total_found, new_count, skip_count, fail_count, error_msg
                FROM crawl_jobs
                ORDER BY started_at DESC
                LIMIT :limit
                """;

        return jdbcClient.sql(sql)
                .param("limit", limit)
                .query((rs, rowNum) -> new CrawlJobDto(
                        rs.getLong("id"),
                        rs.getLong("source_id"),
                        rs.getObject("started_at", java.time.OffsetDateTime.class),
                        rs.getObject("finished_at", java.time.OffsetDateTime.class),
                        rs.getString("status"),
                        rs.getInt("total_found"),
                        rs.getInt("new_count"),
                        rs.getInt("skip_count"),
                        rs.getInt("fail_count"),
                        rs.getString("error_msg")
                ))
                .list();
    }

    public List<ParseLogDto> findRecentParseLogs(int limit) {
        String sql = """
                SELECT id, crawl_job_id, file_key, parser, success,
                       chars_extracted, error_msg, parsed_at
                FROM parse_logs
                ORDER BY parsed_at DESC
                LIMIT :limit
                """;

        return jdbcClient.sql(sql)
                .param("limit", limit)
                .query((rs, rowNum) -> new ParseLogDto(
                        rs.getLong("id"),
                        rs.getObject("crawl_job_id", Long.class),
                        rs.getString("file_key"),
                        rs.getString("parser"),
                        rs.getBoolean("success"),
                        rs.getInt("chars_extracted"),
                        rs.getString("error_msg"),
                        rs.getObject("parsed_at", java.time.OffsetDateTime.class)
                ))
                .list();
    }
}
