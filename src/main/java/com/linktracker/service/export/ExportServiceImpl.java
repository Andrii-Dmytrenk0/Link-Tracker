package com.linktracker.service.export;

import com.linktracker.entity.ClickEvent;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class ExportServiceImpl implements ExportService {

    private static final String[] HEADERS = {
            "Timestamp", "Influencer", "IP", "Country", "City", "Region", "Device",
            "Browser", "OS", "Bot", "Unique", "Suspicious", "Referer"
    };

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    @Override
    public ByteArrayOutputStream toCsv(List<ClickEvent> events) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.println(String.join(",", HEADERS));
            for (ClickEvent e : events) {
                writer.println(String.join(",",
                        csv(FORMATTER.format(e.getTimestamp())),
                        csv(e.getInfluencer() != null ? String.valueOf(e.getInfluencer().getId()) : ""),
                        csv(e.getIp()),
                        csv(e.getCountry()),
                        csv(e.getCity()),
                        csv(e.getRegion()),
                        csv(e.getDeviceType() != null ? e.getDeviceType().name() : ""),
                        csv(e.getBrowser()),
                        csv(e.getOs()),
                        csv(String.valueOf(e.isBot())),
                        csv(String.valueOf(e.isUniqueVisit())),
                        csv(String.valueOf(e.isSuspicious())),
                        csv(e.getReferer())));
            }
        }
        return out;
    }

    @Override
    public ByteArrayOutputStream toExcel(List<ClickEvent> events) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clicks");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ClickEvent e : events) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(FORMATTER.format(e.getTimestamp()));
                row.createCell(1).setCellValue(e.getInfluencer() != null ? e.getInfluencer().getId() : 0);
                row.createCell(2).setCellValue(nullToEmpty(e.getIp()));
                row.createCell(3).setCellValue(nullToEmpty(e.getCountry()));
                row.createCell(4).setCellValue(nullToEmpty(e.getCity()));
                row.createCell(5).setCellValue(nullToEmpty(e.getRegion()));
                row.createCell(6).setCellValue(e.getDeviceType() != null ? e.getDeviceType().name() : "");
                row.createCell(7).setCellValue(nullToEmpty(e.getBrowser()));
                row.createCell(8).setCellValue(nullToEmpty(e.getOs()));
                row.createCell(9).setCellValue(e.isBot());
                row.createCell(10).setCellValue(e.isUniqueVisit());
                row.createCell(11).setCellValue(e.isSuspicious());
                row.createCell(12).setCellValue(nullToEmpty(e.getReferer()));
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        } catch (Exception ex) {
            log.error("Failed to generate Excel export", ex);
        }
        return out;
    }

    @Override
    public ByteArrayOutputStream toPdf(List<ClickEvent> events, String title) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(com.lowagie.text.PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(titleParagraph);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(HEADERS.length);
            table.setWidthPercentage(100);
            Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD);
            for (String header : HEADERS) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
                table.addCell(cell);
            }

            Font cellFont = new Font(Font.HELVETICA, 8);
            for (ClickEvent e : events) {
                table.addCell(new Paragraph(FORMATTER.format(e.getTimestamp()), cellFont));
                table.addCell(new Paragraph(e.getInfluencer() != null ? String.valueOf(e.getInfluencer().getId()) : "", cellFont));
                table.addCell(new Paragraph(nullToEmpty(e.getIp()), cellFont));
                table.addCell(new Paragraph(nullToEmpty(e.getCountry()), cellFont));
                table.addCell(new Paragraph(nullToEmpty(e.getCity()), cellFont));
                table.addCell(new Paragraph(nullToEmpty(e.getRegion()), cellFont));
                table.addCell(new Paragraph(e.getDeviceType() != null ? e.getDeviceType().name() : "", cellFont));
                table.addCell(new Paragraph(nullToEmpty(e.getBrowser()), cellFont));
                table.addCell(new Paragraph(nullToEmpty(e.getOs()), cellFont));
                table.addCell(new Paragraph(String.valueOf(e.isBot()), cellFont));
                table.addCell(new Paragraph(String.valueOf(e.isUniqueVisit()), cellFont));
                table.addCell(new Paragraph(String.valueOf(e.isSuspicious()), cellFont));
                table.addCell(new Paragraph(nullToEmpty(e.getReferer()), cellFont));
            }

            document.add(table);
            document.close();
        } catch (Exception ex) {
            log.error("Failed to generate PDF export", ex);
        }
        return out;
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
