package com.mehmetkerem.service;

/**
 * Fatura PDF oluşturma servisi.
 */
public interface IInvoicePdfService {

    /**
     * Sipariş ID'sine göre fatura PDF'i oluşturur.
     * 
     * @param orderId sipariş ID
     * @return PDF byte dizisi
     */
    byte[] generateInvoicePdf(Long orderId);
}
