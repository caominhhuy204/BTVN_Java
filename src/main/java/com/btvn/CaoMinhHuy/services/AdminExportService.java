package com.btvn.CaoMinhHuy.services;

import com.btvn.CaoMinhHuy.dtos.AdminDashboardData;
import com.btvn.CaoMinhHuy.entities.Book;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminExportService {

    private final AdminDashboardService dashboardService;

    public byte[] exportExcel() {
        AdminDashboardData data = dashboardService.getOverview();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writeSummarySheet(workbook, data);
            writeLatestBooksSheet(workbook, data.latestBooks());
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Không thể xuất Excel", e);
        }
    }

    public byte[] exportWord() {
        AdminDashboardData data = dashboardService.getOverview();
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            createTitle(doc, "Báo cáo doanh thu", 16, true);
            createTitle(doc, "Ngày: " + LocalDate.now(), 11, false);

            addStatParagraph(doc, "Tổng số sách", String.valueOf(data.totalBooks()));
            addStatParagraph(doc, "Doanh thu", formatCurrency(data.totalRevenue()));
            addStatParagraph(doc, "Danh mục", String.valueOf(data.totalCategories()));
            addStatParagraph(doc, "Người dùng", String.valueOf(data.totalUsers()));
            addStatParagraph(doc, "Giá trung bình", formatCurrency(data.averagePrice()));
            addStatParagraph(doc, "Giá thấp nhất", formatCurrency(data.minPrice()));
            addStatParagraph(doc, "Giá cao nhất", formatCurrency(data.maxPrice()));

            addLatestBooksTable(doc, data.latestBooks());

            doc.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Không thể xuất Word", e);
        }
    }

    private void writeSummarySheet(Workbook workbook, AdminDashboardData data) {
        Sheet sheet = workbook.createSheet("Tổng quan");
        int rowIdx = 0;
        rowIdx = createRow(sheet, rowIdx, "Ngày", LocalDate.now().toString());
        rowIdx = createRow(sheet, rowIdx, "Tổng số sách", data.totalBooks());
        rowIdx = createRow(sheet, rowIdx, "Doanh thu", formatCurrency(data.totalRevenue()));
        rowIdx = createRow(sheet, rowIdx, "Danh mục", data.totalCategories());
        rowIdx = createRow(sheet, rowIdx, "Người dùng", data.totalUsers());
        rowIdx = createRow(sheet, rowIdx, "Giá trung bình", formatCurrency(data.averagePrice()));
        rowIdx = createRow(sheet, rowIdx, "Giá thấp nhất", formatCurrency(data.minPrice()));
        createRow(sheet, rowIdx, "Giá cao nhất", formatCurrency(data.maxPrice()));

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void writeLatestBooksSheet(Workbook workbook, List<Book> books) {
        Sheet sheet = workbook.createSheet("Sách mới nhất");
        int rowIdx = 0;
        Row header = sheet.createRow(rowIdx++);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Tiêu đề");
        header.createCell(2).setCellValue("Tác giả");
        header.createCell(3).setCellValue("Giá");
        header.createCell(4).setCellValue("Danh mục");

        if (books != null) {
            for (Book b : books) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(b.getId());
                row.createCell(1).setCellValue(b.getTitle());
                row.createCell(2).setCellValue(b.getAuthor());
                row.createCell(3).setCellValue(b.getPrice() != null ? b.getPrice() : 0);
                row.createCell(4).setCellValue(b.getCategory() != null ? b.getCategory().getName() : "-");
            }
        }

        for (int i = 0; i <= 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private int createRow(Sheet sheet, int rowIndex, String label, Object value) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value != null ? value.toString() : "");
        return rowIndex + 1;
    }

    private void createTitle(XWPFDocument doc, String text, int fontSize, boolean bold) {
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(fontSize);
    }

    private void addStatParagraph(XWPFDocument doc, String label, String value) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setBold(true);
        run.setText(label + ": ");

        XWPFRun valueRun = p.createRun();
        valueRun.setText(value != null ? value : "-");
    }

    private void addLatestBooksTable(XWPFDocument doc, List<Book> books) {
        XWPFParagraph title = doc.createParagraph();
        title.setSpacingBefore(200);
        XWPFRun run = title.createRun();
        run.setBold(true);
        run.setFontSize(13);
        run.setText("Sách mới nhất");

        int rows = (books == null || books.isEmpty()) ? 2 : books.size() + 1;
        XWPFTable table = doc.createTable(rows, 5);

        table.getRow(0).getCell(0).setText("ID");
        table.getRow(0).getCell(1).setText("Tiêu đề");
        table.getRow(0).getCell(2).setText("Tác giả");
        table.getRow(0).getCell(3).setText("Giá");
        table.getRow(0).getCell(4).setText("Danh mục");

        if (books == null || books.isEmpty()) {
            table.getRow(1).getCell(0).setText("-");
            table.getRow(1).getCell(1).setText("Chưa có dữ liệu");
            table.getRow(1).getCell(2).setText("-");
            table.getRow(1).getCell(3).setText("-");
            table.getRow(1).getCell(4).setText("-");
            return;
        }

        int rowIdx = 1;
        for (Book b : books) {
            XWPFTableRow row = table.getRow(rowIdx++);
            row.getCell(0).setText(String.valueOf(b.getId()));
            row.getCell(1).setText(b.getTitle());
            row.getCell(2).setText(b.getAuthor());
            row.getCell(3).setText(formatCurrency(b.getPrice()));
            row.getCell(4).setText(b.getCategory() != null ? b.getCategory().getName() : "-");
        }
    }

    private String formatCurrency(Double value) {
        if (value == null) return "0";
        return String.format("%.2f", value);
    }
}
