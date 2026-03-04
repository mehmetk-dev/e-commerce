package com.mehmetkerem.enums;

/**
 * Kullanıcı aktivite türleri — admin paneli için detaylı loglama.
 */
public enum ActivityType {

    // Auth
    LOGIN,
    LOGOUT,
    REGISTER,
    PASSWORD_RESET_REQUEST,
    PASSWORD_RESET_COMPLETE,

    // Ürün
    PRODUCT_VIEW,

    // Sepet
    CART_ADD_ITEM,
    CART_REMOVE_ITEM,
    CART_UPDATE_QUANTITY,
    CART_CLEAR,
    CART_APPLY_COUPON,
    CART_REMOVE_COUPON,

    // Sipariş
    ORDER_CREATE,
    ORDER_CANCEL,
    ORDER_STATUS_UPDATE,
    ORDER_TRACKING_UPDATE,

    // Ödeme
    PAYMENT_PROCESS,
    PAYMENT_SUCCESS,
    PAYMENT_FAIL,

    // İade
    RETURN_REQUEST,
    RETURN_APPROVE,
    RETURN_REJECT,

    // Yorum
    REVIEW_CREATE,
    REVIEW_UPDATE,
    REVIEW_DELETE,

    // Favori
    WISHLIST_ADD,
    WISHLIST_REMOVE,
    WISHLIST_CLEAR,

    // Adres
    ADDRESS_CREATE,
    ADDRESS_UPDATE,
    ADDRESS_DELETE,

    // Destek
    TICKET_CREATE,
    TICKET_REPLY,

    // Profil
    PROFILE_UPDATE
}
