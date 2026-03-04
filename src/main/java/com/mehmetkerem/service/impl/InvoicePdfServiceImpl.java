package com.mehmetkerem.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mehmetkerem.dto.response.OrderInvoiceResponse;
import com.mehmetkerem.service.IInvoicePdfService;
import com.mehmetkerem.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@lombok.RequiredArgsConstructor
public class InvoicePdfServiceImpl implements IInvoicePdfService {

    private final IOrderService orderService;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(44, 62, 80));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
    private static final Color PRIMARY_COLOR = new Color(52, 73, 94);
    private static final Color STRIPE_COLOR = new Color(241, 245, 249);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(0.18);

    @Override
    public byte[] generateInvoicePdf(Long orderId) {
        OrderInvoiceResponse invoice = orderService.getOrderInvoice(orderId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document, invoice);
            addCustomerInfo(document, invoice);
            addItemsTable(document, invoice);
            addTotals(document, invoice);
            addFooter(document);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Fatura PDF olusturulamadi: {}", e.getMessage(), e);
            throw new com.mehmetkerem.exception.BadRequestException("PDF oluşturma hatası: " + e.getMessage());
        }
    }

    private void addHeader(Document document, OrderInvoiceResponse invoice) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[] { 60, 40 });

        // Sol: Firma adı
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(0);
        leftCell.addElement(new Phrase("CAN ANTIKA", TITLE_FONT));
        leftCell.addElement(new Phrase("E-Ticaret Faturası", SMALL_FONT));
        headerTable.addCell(leftCell);

        // Sağ: Fatura bilgileri
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(0);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(new Phrase("Fatura No: " + invoice.getInvoiceNumber(), BOLD_FONT));
        rightCell.addElement(new Phrase("Tarih: " + invoice.getOrderDate().format(DATE_FMT), BODY_FONT));
        rightCell.addElement(new Phrase("Durum: " + invoice.getOrderStatus(), BODY_FONT));
        headerTable.addCell(rightCell);

        document.add(headerTable);
        document.add(new Paragraph(" "));

        // Çizgi
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorderWidthBottom(2);
        lineCell.setBorderColorBottom(PRIMARY_COLOR);
        lineCell.setBorderWidthTop(0);
        lineCell.setBorderWidthLeft(0);
        lineCell.setBorderWidthRight(0);
        lineCell.setFixedHeight(5);
        line.addCell(lineCell);
        document.add(line);
        document.add(new Paragraph(" "));
    }

    private void addCustomerInfo(Document document, OrderInvoiceResponse invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 50, 50 });

        // Müşteri
        PdfPCell customerCell = new PdfPCell();
        customerCell.setBorder(0);
        customerCell.addElement(new Phrase("Müşteri Bilgileri", BOLD_FONT));
        customerCell.addElement(new Phrase(invoice.getCustomerName(), BODY_FONT));
        table.addCell(customerCell);

        // Adres
        PdfPCell addressCell = new PdfPCell();
        addressCell.setBorder(0);
        addressCell.addElement(new Phrase("Teslimat Adresi", BOLD_FONT));
        addressCell.addElement(new Phrase(invoice.getShippingAddressSummary(), BODY_FONT));
        table.addCell(addressCell);

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addItemsTable(Document document, OrderInvoiceResponse invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 45, 15, 20, 20 });

        // Başlık satırı
        addTableHeader(table, "Ürün");
        addTableHeader(table, "Adet");
        addTableHeader(table, "Birim Fiyat");
        addTableHeader(table, "Toplam");

        // Satırlar
        boolean striped = false;
        for (OrderInvoiceResponse.InvoiceItemLine item : invoice.getItems()) {
            Color bgColor = striped ? STRIPE_COLOR : Color.WHITE;
            addTableCell(table, item.getProductTitle(), BODY_FONT, bgColor, Element.ALIGN_LEFT);
            addTableCell(table, String.valueOf(item.getQuantity()), BODY_FONT, bgColor, Element.ALIGN_CENTER);
            addTableCell(table, formatPrice(item.getUnitPrice()), BODY_FONT, bgColor, Element.ALIGN_RIGHT);
            addTableCell(table, formatPrice(item.getLineTotal()), BOLD_FONT, bgColor, Element.ALIGN_RIGHT);
            striped = !striped;
        }

        document.add(table);
    }

    private void addTotals(Document document, OrderInvoiceResponse invoice) throws DocumentException {
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addTotalRow(table, "Ara Toplam:", formatPrice(invoice.getSubtotal()));
        addTotalRow(table, "KDV (%18):",
                formatPrice(invoice.getSubtotal().multiply(VAT_RATE)));

        // GENEL TOPLAM
        PdfPCell labelCell = new PdfPCell(
                new Phrase("GENEL TOPLAM:", new Font(Font.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR)));
        labelCell.setBorder(0);
        labelCell.setBorderWidthTop(1);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(formatPrice(invoice.getTotalAmount()),
                new Font(Font.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR)));
        valueCell.setBorder(0);
        valueCell.setBorderWidthTop(1);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);

        document.add(table);
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        Paragraph footer = new Paragraph(
                "Bu fatura elektronik ortamda oluşturulmuştur.\n" +
                        "Can Antika E-Ticaret | info@canantika.com\n" +
                        "İade ve değişim koşulları için web sitemizi ziyaret ediniz.",
                SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    // ── Helper ──

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setBorderColor(new Color(220, 220, 220));
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(0);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, BODY_FONT));
        valueCell.setBorder(0);
        valueCell.setPadding(4);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String formatPrice(java.math.BigDecimal price) {
        if (price == null)
            return "0,00 ₺";
        return String.format("%,.2f ₺", price);
    }
}
