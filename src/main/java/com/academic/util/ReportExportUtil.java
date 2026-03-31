package com.academic.util;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for exporting generated reports as CSV and PDF files.
 */
public final class ReportExportUtil {

    private static final float PDF_MARGIN = 50f;
    private static final float PDF_FONT_SIZE = 10f;
    private static final float PDF_LINE_HEIGHT = 14f;
    private static final int PDF_MAX_CHARS_PER_LINE = 95;

    private ReportExportUtil() {
        // Utility class
    }

    public static boolean exportReportAsCsv(Stage owner, String reportType, String reportText) {
        if (reportText == null || reportText.isBlank()) {
            throw new IllegalArgumentException("No report content to export.");
        }

        FileChooser chooser = createFileChooser("csv", "CSV Files", "*.csv", reportType);
        Path target = toPath(chooser.showSaveDialog(owner));
        if (target == null) {
            return false;
        }

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(target, StandardCharsets.UTF_8))) {
            writer.println("Report Type,Generated Date,Line Number,Content");
            String safeType = escapeCsv(reportType == null ? "Report" : reportType);
            String generatedDate = LocalDate.now().toString();
            String[] lines = reportText.replace("\r\n", "\n").split("\n", -1);

            for (int i = 0; i < lines.length; i++) {
                writer.printf("%s,%s,%d,%s%n",
                    safeType,
                    generatedDate,
                    i + 1,
                    escapeCsv(lines[i])
                );
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to export CSV report.", ex);
        }

        return true;
    }

    public static boolean exportReportAsPdf(Stage owner, String reportType, String reportText) {
        if (reportText == null || reportText.isBlank()) {
            throw new IllegalArgumentException("No report content to export.");
        }

        FileChooser chooser = createFileChooser("pdf", "PDF Files", "*.pdf", reportType);
        Path target = toPath(chooser.showSaveDialog(owner));
        if (target == null) {
            return false;
        }

        try (PDDocument document = new PDDocument()) {
            List<String> lines = wrapReportLines(reportText);

            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.COURIER);
            content.setFont(font, PDF_FONT_SIZE);
            content.beginText();
            content.newLineAtOffset(PDF_MARGIN, page.getMediaBox().getHeight() - PDF_MARGIN);

            float y = page.getMediaBox().getHeight() - PDF_MARGIN;
            for (String line : lines) {
                if (y <= PDF_MARGIN) {
                    content.endText();
                    content.close();

                    page = new PDPage(PDRectangle.LETTER);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    content.setFont(font, PDF_FONT_SIZE);
                    content.beginText();
                    content.newLineAtOffset(PDF_MARGIN, page.getMediaBox().getHeight() - PDF_MARGIN);
                    y = page.getMediaBox().getHeight() - PDF_MARGIN;
                }

                content.showText(sanitizePdfLine(line));
                content.newLineAtOffset(0, -PDF_LINE_HEIGHT);
                y -= PDF_LINE_HEIGHT;
            }

            content.endText();
            content.close();

            document.save(target.toFile());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to export PDF report.", ex);
        }

        return true;
    }

    private static FileChooser createFileChooser(String extension,
                                                 String description,
                                                 String glob,
                                                 String reportType) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Report as " + extension.toUpperCase());
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, glob));
        chooser.setInitialFileName(buildDefaultFileName(reportType, extension));
        return chooser;
    }

    private static String buildDefaultFileName(String reportType, String extension) {
        String base = reportType == null || reportType.isBlank()
            ? "report"
            : reportType.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "report";
        }
        return base + "-" + LocalDate.now() + "." + extension;
    }

    private static Path toPath(java.io.File selectedFile) {
        return selectedFile == null ? null : selectedFile.toPath();
    }

    private static String escapeCsv(String input) {
        String value = input == null ? "" : input;
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private static List<String> wrapReportLines(String reportText) {
        String[] lines = reportText.replace("\r\n", "\n").split("\n", -1);
        List<String> wrapped = new ArrayList<>();

        for (String line : lines) {
            if (line.length() <= PDF_MAX_CHARS_PER_LINE) {
                wrapped.add(line);
                continue;
            }

            int start = 0;
            while (start < line.length()) {
                int end = Math.min(start + PDF_MAX_CHARS_PER_LINE, line.length());
                wrapped.add(line.substring(start, end));
                start = end;
            }
        }

        return wrapped;
    }

    private static String sanitizePdfLine(String line) {
        return (line == null ? "" : line)
            .replace('\t', ' ')
            .replace('\u0000', ' ');
    }
}