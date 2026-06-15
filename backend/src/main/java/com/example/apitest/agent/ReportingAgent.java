package com.example.apitest.agent;

import com.example.apitest.service.LlmGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Generates an HTML (or PDF/Excel later) report using the Qwen model.
 * For now it builds a simple HTML page with the key results of the run.
 */
@Component
public class ReportingAgent implements LangGraphAgent {

    private final LlmGatewayService llmService;

    @Autowired
    public ReportingAgent(LlmGatewayService llmService) {
        this.llmService = llmService;
    }

    @Override
    public Object run(Map<String, Object> context) {
        String validation = (String) context.getOrDefault("validationResult", "{}");
        String failure = (String) context.getOrDefault("failureReport", "");
        String jira = (String) context.getOrDefault("jiraTicket", "None");
        String suggestion = (String) context.getOrDefault("optimizationSuggestion", "");
        String prompt = """
            You are a technical writer creating an executive HTML report for an API-testing pipeline run. Produce a well-structured HTML document (as a raw string, no markdown) that includes the following sections with clear headings and, where appropriate, HTML tables:

            1. Summary - a brief one-sentence overview of the run outcome.
            2. Validation Results - display the JSON validation object (validationResult) in a <pre> block and highlight pass/fail status.
            3. Execution Summary - show the raw execution result (executionResult) in a <pre> block.
            4. Failure Analysis - if a failure report exists, render it in a <pre> block; otherwise state "No failures detected".
            5. Jira Ticket - list the created ticket key or "None".
            6. Optimization Suggestion - display the suggestion string.
            7. Timestamp - include the current UTC date-time.

            Return only the final HTML string.

            Validation Result:
            """ + validation + """

            Execution Result:
            """ + context.getOrDefault("executionResult", "") + """

            Failure Analysis:
            """ + failure + """

            Jira Ticket:
            """ + jira + """

            Optimization Suggestion:
            """ + suggestion;
        // Generate HTML report via LLM
        String html = llmService.complete("llama-3.1-8b-instant", prompt);
        context.put("htmlReport", html);
        // Also generate PDF and Excel versions of the report for download
        try {
            // PDF generation using iText
            byte[] pdfBytes = generatePdfReport(html);
            String pdfBase64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);
            context.put("pdfReportBase64", pdfBase64);
            // Excel generation using Apache POI
            byte[] excelBytes = generateExcelReport(html);
            String excelBase64 = java.util.Base64.getEncoder().encodeToString(excelBytes);
            context.put("excelReportBase64", excelBase64);
        } catch (Exception e) {
            // In a production system you would log this error
            context.put("pdfReportBase64", "");
            context.put("excelReportBase64", "");
        }
        return html;
    }

    /**
     * Simple PDF generation from plain HTML/text using iText.
     * For this prototype we just embed the raw HTML string as plain text.
     */
    private byte[] generatePdfReport(String htmlContent) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
        com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);
        document.add(new com.itextpdf.layout.element.Paragraph("API Testing Report"));
        document.add(new com.itextpdf.layout.element.Paragraph(htmlContent));
        document.close();
        return baos.toByteArray();
    }

    /**
     * Simple Excel generation using Apache POI.
     * Splits the HTML content into lines and puts each line into a new row.
     */
    private byte[] generateExcelReport(String htmlContent) throws Exception {
        org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Report");
        String[] lines = htmlContent.split("\n");
        for (int i = 0; i < lines.length; i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(i);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            cell.setCellValue(lines[i]);
        }
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }
}