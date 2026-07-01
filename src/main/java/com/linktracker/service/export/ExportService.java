package com.linktracker.service.export;

import com.linktracker.entity.ClickEvent;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Generates downloadable exports of click statistics in multiple formats.
 */
public interface ExportService {

    ByteArrayOutputStream toCsv(List<ClickEvent> events);

    ByteArrayOutputStream toExcel(List<ClickEvent> events);

    ByteArrayOutputStream toPdf(List<ClickEvent> events, String title);
}
