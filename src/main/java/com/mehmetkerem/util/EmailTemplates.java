package com.mehmetkerem.util;

/**
 * Can Antika e-posta HTML şablonları.
 * Resend ile gönderilecek e-postalar için premium tasarımlı şablonlar.
 */
public final class EmailTemplates {

  private EmailTemplates() {
  }

  private static String wrap(String title, String body) {
    return """
        <!DOCTYPE html>
        <html lang="tr">
        <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
        <body style="margin:0;padding:0;background-color:#f5f0eb;font-family:'Georgia',serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f5f0eb;padding:40px 0;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                <!-- Header -->
                <tr>
                  <td style="background-color:#1a1a2e;padding:32px 40px;text-align:center;">
                    <h1 style="margin:0;color:#d4a574;font-size:28px;font-weight:600;letter-spacing:1px;">Can Antika</h1>
                    <p style="margin:8px 0 0;color:#a0a0b8;font-size:12px;letter-spacing:3px;text-transform:uppercase;">EST. 1989 · İSTANBUL</p>
                  </td>
                </tr>
                <!-- Title -->
                <tr>
                  <td style="padding:32px 40px 16px;text-align:center;">
                    <h2 style="margin:0;color:#1a1a2e;font-size:22px;font-weight:600;">%s</h2>
                  </td>
                </tr>
                <!-- Body -->
                <tr>
                  <td style="padding:0 40px 32px;">
                    %s
                  </td>
                </tr>
                <!-- Footer -->
                <tr>
                  <td style="background-color:#faf8f5;padding:24px 40px;text-align:center;border-top:1px solid #e8e0d8;">
                    <p style="margin:0;color:#8a8078;font-size:12px;">Çukurcuma Caddesi No: 45, Beyoğlu, İstanbul</p>
                    <p style="margin:4px 0 0;color:#8a8078;font-size:12px;">+90 (212) 555-0123 · info@canantika.com</p>
                    <p style="margin:12px 0 0;color:#b0a898;font-size:11px;font-style:italic;">"Geçmişin izinde, geleceğe miras"</p>
                  </td>
                </tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """
        .formatted(title, body);
  }

  public static String orderConfirmation(String orderCode) {
    String body = """
        <div style="text-align:center;padding:16px 0;">
          <div style="display:inline-block;background-color:#f0ebe4;border-radius:50%%;padding:20px;margin-bottom:16px;">
            <span style="font-size:36px;">✓</span>
          </div>
          <p style="color:#4a4a4a;font-size:16px;line-height:1.6;margin:16px 0;">Siparişiniz başarıyla alındı ve işleme konuldu.</p>
          <div style="background-color:#faf8f5;border:1px solid #e8e0d8;border-radius:8px;padding:20px;margin:16px 0;">
            <p style="margin:0;color:#8a8078;font-size:13px;">Sipariş Kodunuz</p>
            <p style="margin:4px 0 0;color:#1a1a2e;font-size:24px;font-weight:700;letter-spacing:2px;">%s</p>
          </div>
          <p style="color:#6a6a6a;font-size:14px;line-height:1.6;">Siparişinizin durumunu hesabınızdan takip edebilirsiniz.</p>
        </div>
        """
        .formatted(orderCode);
    return wrap("Siparişiniz Alındı!", body);
  }

  public static String welcome(String name) {
    String body = """
        <div style="text-align:center;padding:16px 0;">
          <p style="color:#4a4a4a;font-size:16px;line-height:1.6;margin:0 0 16px;">
            Merhaba <strong>%s</strong>,
          </p>
          <p style="color:#4a4a4a;font-size:16px;line-height:1.6;margin:0 0 24px;">
            Can Antika ailesine hoş geldiniz! 35 yılı aşkın tecrübemizle sizlere en seçkin antika eserleri sunmaktan gurur duyuyoruz.
          </p>
          <div style="border-top:1px solid #e8e0d8;border-bottom:1px solid #e8e0d8;padding:20px 0;margin:24px 0;">
            <p style="color:#8a8078;font-size:14px;margin:0;"><strong style="color:#1a1a2e;">Keşfedin:</strong> Osmanlı, Viktoryen, Art Deco ve daha fazlası</p>
          </div>
          <a href="https://canantika.com/urunler" style="display:inline-block;background-color:#1a1a2e;color:#d4a574;text-decoration:none;padding:14px 32px;border-radius:6px;font-size:14px;font-weight:600;letter-spacing:1px;">KOLEKSİYONU KEŞFET</a>
        </div>
        """
        .formatted(name);
    return wrap("Hoş Geldiniz!", body);
  }

  public static String passwordReset(String resetUrl) {
    String body = """
        <div style="text-align:center;padding:16px 0;">
          <p style="color:#4a4a4a;font-size:16px;line-height:1.6;margin:0 0 24px;">
            Şifrenizi sıfırlamak için aşağıdaki butona tıklayın. Bu link 24 saat geçerlidir.
          </p>
          <a href="%s" style="display:inline-block;background-color:#1a1a2e;color:#d4a574;text-decoration:none;padding:14px 32px;border-radius:6px;font-size:14px;font-weight:600;letter-spacing:1px;">ŞİFREMİ SIFIRLA</a>
          <p style="color:#8a8078;font-size:13px;margin:24px 0 0;">Bu işlemi siz talep etmediyseniz bu e-postayı görmezden gelebilirsiniz.</p>
        </div>
        """
        .formatted(resetUrl);
    return wrap("Şifre Sıfırlama", body);
  }

  public static String orderTracking(String orderCode, String trackingNumber, String carrier) {
    String body = """
        <div style="text-align:center;padding:16px 0;">
          <div style="display:inline-block;background-color:#f0ebe4;border-radius:50%%;padding:20px;margin-bottom:16px;">
            <span style="font-size:36px;">📦</span>
          </div>
          <p style="color:#4a4a4a;font-size:16px;line-height:1.6;margin:16px 0;">Siparişiniz kargoya verildi!</p>
          <div style="background-color:#faf8f5;border:1px solid #e8e0d8;border-radius:8px;padding:20px;margin:16px 0;">
            <table width="100%%" cellpadding="8" cellspacing="0" style="font-size:14px;">
              <tr><td style="color:#8a8078;text-align:right;width:50%%;">Sipariş:</td><td style="color:#1a1a2e;font-weight:600;">%s</td></tr>
              <tr><td style="color:#8a8078;text-align:right;">Kargo Firması:</td><td style="color:#1a1a2e;font-weight:600;">%s</td></tr>
              <tr><td style="color:#8a8078;text-align:right;">Takip No:</td><td style="color:#1a1a2e;font-weight:600;letter-spacing:1px;">%s</td></tr>
            </table>
          </div>
        </div>
        """
        .formatted(orderCode, carrier, trackingNumber);
    return wrap("Siparişiniz Yola Çıktı!", body);
  }

  public static String stockAlert(String productName, int currentStock) {
    String body = """
        <div style="text-align:center;padding:16px 0;">
          <div style="display:inline-block;background-color:#fef2f2;border-radius:50%%;padding:20px;margin-bottom:16px;">
            <span style="font-size:36px;">⚠️</span>
          </div>
          <p style="color:#4a4a4a;font-size:16px;line-height:1.6;margin:16px 0;">
            Aşağıdaki ürünün stoku kritik seviyeye düştü.
          </p>
          <div style="background-color:#fef2f2;border:1px solid #fecaca;border-radius:8px;padding:20px;margin:16px 0;">
            <p style="margin:0;color:#8a8078;font-size:13px;">Ürün</p>
            <p style="margin:4px 0 0;color:#1a1a2e;font-size:18px;font-weight:700;">%s</p>
            <p style="margin:12px 0 0;color:#dc2626;font-size:24px;font-weight:700;">Kalan stok: %d</p>
          </div>
          <p style="color:#6a6a6a;font-size:14px;">Lütfen stok güncellemesi yapınız.</p>
        </div>
        """
        .formatted(productName, currentStock);
    return wrap("Stok Uyarısı", body);
  }

  public static String orderStatusUpdate(String orderCode, String statusLabel) {
    String body = """
        <div style="text-align:center;padding:16px 0;">
          <p style="color:#4a4a4a;font-size:16px;line-height:1.6;margin:0 0 16px;">
            <strong>%s</strong> numaralı siparişinizin durumu güncellendi.
          </p>
          <div style="background-color:#faf8f5;border:1px solid #e8e0d8;border-radius:8px;padding:20px;margin:16px 0;">
            <p style="margin:0;color:#8a8078;font-size:13px;">Yeni Durum</p>
            <p style="margin:8px 0 0;color:#1a1a2e;font-size:20px;font-weight:700;">%s</p>
          </div>
          <p style="color:#6a6a6a;font-size:14px;">Detaylar için hesabınızı kontrol edebilirsiniz.</p>
        </div>
        """
        .formatted(orderCode, statusLabel);
    return wrap("Sipariş Durumu Güncellendi", body);
  }
}
