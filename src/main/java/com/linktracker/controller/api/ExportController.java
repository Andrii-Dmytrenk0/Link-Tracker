package com.linktracker.controller.api;

import com.linktracker.entity.ClickEvent;
import com.linktracker.repository.ClickEventRepository;
import com.linktracker.repository.ClickEventSpecifications;
import com.linktracker.service.export.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * Exports click statistics as CSV, Excel (.xlsx) or PDF, with optional
 * filtering by influencer, date range, country and city. Filters are
 * applied via {@link ClickEventSpecifications}, which builds the query
 * predicates dynamically in Java so that an absent filter never becomes a
 * SQL bind parameter (see the note on {@link ClickEventRepository} for why
 * that matters on PostgreSQL).
 */
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "CSV / Excel / PDF export of click statistics")
public class ExportController {

    private final ClickEventRepository clickEventRepository;
    private final ExportService exportService;

    @GetMapping(value = "/csv", produces = "text/csv")
    @Operation(summary = "Export filtered click events as CSV")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) Long influencerId,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                             @RequestParam(required = false) String country,
                                             @RequestParam(required = false) String city) {
        List<ClickEvent> events = findEvents(influencerId, from, to, country, city);
        byte[] data = exportService.toCsv(events).toByteArray();
        return fileResponse(data, "clicks-export.csv", "text/csv");
    }

    @GetMapping(value = "/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export filtered click events as an Excel (.xlsx) workbook")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) Long influencerId,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                               @RequestParam(required = false) String country,
                                               @RequestParam(required = false) String city) {
        List<ClickEvent> events = findEvents(influencerId, from, to, country, city);
        byte[] data = exportService.toExcel(events).toByteArray();
        return fileResponse(data, "clicks-export.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Export filtered click events as a PDF report")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) Long influencerId,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                             @RequestParam(required = false) String country,
                                             @RequestParam(required = false) String city) {
        List<ClickEvent> events = findEvents(influencerId, from, to, country, city);
        byte[] data = exportService.toPdf(events, "Click Statistics Report").toByteArray();
        return fileResponse(data, "clicks-export.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    private List<ClickEvent> findEvents(Long influencerId, Instant from, Instant to, String country, String city) {
        return clickEventRepository.findAll(
                ClickEventSpecifications.withFilters(influencerId, from, to, country, city));
    }

    private ResponseEntity<byte[]> fileResponse(byte[] data, String filename, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
