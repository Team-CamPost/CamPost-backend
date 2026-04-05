package com.campost.backend.domain.collect.importer.service;

import com.campost.backend.domain.collect.importer.model.RawNoticePayload;
import com.campost.backend.domain.collect.importer.repository.RawImporterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
public class RawImportTxService {

    private final RawImporterRepository rawImporterRepository;

    public RawImportTxService(RawImporterRepository rawImporterRepository) {
        this.rawImporterRepository = rawImporterRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void importOne(
            String fileName,
            RawNoticePayload payload,
            OffsetDateTime crawledAt,
            LocalDate noticeDate,
            Integer views,
            LocalDate deadline
    ) {
        long rawNoticeId = rawImporterRepository.upsertRawNotice(payload, crawledAt);
        rawImporterRepository.upsertNotice(rawNoticeId, payload, noticeDate, views, deadline, crawledAt);
        rawImporterRepository.logImport(fileName, "SUCCESS", "Imported to raw_notices/notices");
    }
}
