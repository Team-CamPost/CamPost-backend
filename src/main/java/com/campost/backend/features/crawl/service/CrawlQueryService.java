package com.campost.backend.features.crawl.service;

import com.campost.backend.features.crawl.dto.CrawlJobDto;
import com.campost.backend.features.crawl.dto.ParseLogDto;
import com.campost.backend.features.crawl.repository.CrawlQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrawlQueryService {

    private static final int MAX_LIMIT = 100;

    private final CrawlQueryRepository crawlQueryRepository;

    public CrawlQueryService(CrawlQueryRepository crawlQueryRepository) {
        this.crawlQueryRepository = crawlQueryRepository;
    }

    public List<CrawlJobDto> getRecentCrawlJobs(int limit) {
        int safeLimit = normalizeLimit(limit);
        return crawlQueryRepository.findRecentCrawlJobs(safeLimit);
    }

    public List<ParseLogDto> getRecentParseLogs(int limit) {
        int safeLimit = normalizeLimit(limit);
        return crawlQueryRepository.findRecentParseLogs(safeLimit);
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 20;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
