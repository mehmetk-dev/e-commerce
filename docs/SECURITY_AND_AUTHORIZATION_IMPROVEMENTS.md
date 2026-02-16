# Güvenlik ve Yetkilendirme İyileştirmeleri

Bu dokümanda e-ticaret backend'inde yapılan güvenlik ve yetkilendirme değişiklikleri, **neden** yapıldıkları ve **ne işe yaradıkları** junior developer seviyesinde anlatılmaktadır.

---

## 1. Sorun: IDOR (Insecure Direct Object Reference) Nedir?

**Ne demek?**  
API'de bir kaynağa (sepet, sipariş, adres vb.) erişirken **kim olduğumuz kontrol edilmiyorsa**, başka bir kullanıcının verisine erişebiliriz. Buna **IDOR** denir.

**Eski davranış (örnek):**
- İstek: `GET /v1/cart/5` → 5 numaralı kullanıcının sepeti dönüyordu.
- Herhangi biri, URL'deki sayıyı değiştirerek (5 → 10, 20 …) **başka kullanıcıların sepetini** görebiliyordu.
- Aynı mantık sipariş, wishlist ve ödeme endpoint'lerinde de vardı: path'te `userId` vardı ama "bu isteği yapan kişi gerçekten bu userId mi?" diye bakılmıyordu.

**Özet:** Path'teki `userId` ile **oturum açmış kullanıcı** eşleştirilmediği için yetkisiz erişim riski vardı.

---

## 2. Çözüm Yaklaşımı: "Kim olduğunu token'dan al"

- Artık **kim olduğumuz** path'ten değil, **JWT token** (Authorization header) üzerinden alınıyor.
- Backend, token'dan kullanıcıyı çözüp `SecurityContext`'e koyuyor; biz de bu kullanıcının ID'sini kullanıyoruz.
- Böylece "sepete ekle" dediğimizde **her zaman kendi sepetimize** ekleniyor; başkasının ID'sini path'e yazmak işe yaramıyor.

Bunu kolay kullanmak için ortak bir yardımcı sınıf eklendi.

---

## 3. Yapılan Değişiklikler (Adım Adım)

### 3.1. `SecurityUtils` sınıfı

**Ne yaptık?**  
`com.mehmetkerem.util.SecurityUtils` adında bir sınıf ekledik.

**Ne işe yarıyor?**
- `SecurityUtils.getCurrentUser()` → Oturum açmış kullanıcıyı (`User` entity) döner.
- `SecurityUtils.getCurrentUserId()` → Oturum açmış kullanıcının ID'sini (`Long`) döner.

**Neden?**  
Controller'larda her seferinde `SecurityContextHolder.getContext().getAuthentication()...` yazmak tekrarlı ve hataya açıktı. Tek bir yerden "şu an kim giriş yapmış?" diye sormak hem okunabilirliği hem güvenliği artırır.

**Kullanım örneği:**
```java
Long userId = SecurityUtils.getCurrentUserId();
if (userId == null) {
    throw new InsufficientAuthenticationException("Oturum gerekli");
}
// Artık userId ile sadece bu kullanıcıya ait işlem yapılır
```

---

### 3.2. Cart (Sepet) API’si

**Eski:**
- `GET /v1/cart/{userId}`
- `POST /v1/cart/{userId}/items`
- vb. Tüm endpoint'lerde path'te `userId` vardı.

**Yeni:**
- `GET /v1/cart` → Oturum açan kullanıcının sepeti.
- `POST /v1/cart/items` → Oturum açan kullanıcının sepetine ürün eklenir.
- `PUT /v1/cart/items/{productId}`, `DELETE /v1/cart/items/{productId}`, `DELETE /v1/cart`, `GET /v1/cart/total`, kupon ekleme/çıkarma da aynı mantıkla; path'te **artık userId yok**.

**Neden?**  
Böylece bir kullanıcı sadece **kendi** sepetine erişebilir. Başka birinin ID'sini URL'e yazmak anlamsız; backend zaten "şu an kim giriş yaptıysa onun sepeti" diye bakıyor.

---

### 3.3. Order (Sipariş) API’si

**Yapılanlar:**

1. **Path'ten userId kaldırıldı**
   - Eski: `POST /v1/order/save/{userId}`
   - Yeni: `POST /v1/order/save`  
   Siparişi oluşturan kullanıcı, token'dan alınıyor.

2. **"Siparişlerim" endpoint’i eklendi**
   - `GET /v1/order/my-orders` → Giriş yapan kullanıcının sipariş listesi.  
   Böylece müşteri kendi siparişlerini görebiliyor; önceden sadece admin "tüm siparişler" listesine sahipti.

3. **Adres sahipliği kontrolü**
   - Sipariş oluştururken gönderilen `addressId`'nin **gerçekten o kullanıcıya ait** olup olmadığı kontrol ediliyor.
   - Eski: `addressService.getAddressById(addressId)` → Herhangi bir adres alınabiliyordu.
   - Yeni: `addressService.getAddressByIdAndUserId(addressId, userId)` → Sadece bu kullanıcıya ait adres kabul ediliyor; değilse hata.

4. **Ödeme durumu (paymentStatus) güvenliği**
   - Eski: İstemci `paymentStatus: PAID` göndererek siparişi "ödenmiş" gösterebiliyordu.
   - Yeni: Sipariş **her zaman** sunucuda `PENDING` ile oluşturuluyor. Gerçek ödeme, ödeme akışı (payment service / webhook) tamamlandığında güncellenir. İstemci bu alanı artık belirleyemez.

5. **Admin-only endpoint’ler**
   - `GET /v1/order/all` (tüm siparişler) ve `PUT /v1/order/{orderId}/tracking` (kargo bilgisi) sadece **ADMIN** rolüne açıldı (`@PreAuthorize("hasRole('ADMIN')")`).  
   Böylece normal kullanıcı tüm siparişleri listeleyemez veya kargo bilgisi güncelleyemez.

---

### 3.4. Wishlist (Favoriler) API’si

**Eski:** Path'te `userId` vardı (örn. `GET /v1/wishlist/{userId}`).

**Yeni:** Path'te userId yok; kullanıcı token'dan alınıyor.
- `GET /v1/wishlist`
- `POST /v1/wishlist/add/{productId}`
- `DELETE /v1/wishlist/remove/{productId}`
- `DELETE /v1/wishlist/clear`

**Neden?**  
Yine IDOR riskini kaldırmak: Bir kullanıcı sadece kendi wishlist'ine erişebilir.

---

### 3.5. Payment (Ödeme) API’si

**Yapılanlar:**

1. **Path/param’dan userId kaldırıldı**
   - Eski: `POST /v1/payment/process?userId=5&orderId=10&...`
   - Yeni: `POST /v1/payment/process?orderId=10&...`  
   userId artık token'dan alınıyor.

2. **Sipariş sahipliği kontrolü**
   - "Bu sipariş, bu kullanıcıya mı ait?" kontrolü eklendi.  
   Başka kullanıcının siparişi için ödeme işlemi yapılamaz; yapılırsa anlamlı bir hata dönülür.

3. **Kullanıcının kendi ödemeleri**
   - Eski: `GET /v1/payment/user/{userId}` → Yine path'te userId.
   - Yeni: `GET /v1/payment/my-payments` → Giriş yapan kullanıcının ödemeleri.

---

### 3.6. Address (Adres) API’si

**Yapılanlar:**

1. **Kendi adresine erişim**
   - `GET /v1/address/{id}` artık sadece **o adres, giriş yapan kullanıcıya aitse** dönüyor.  
   Servis tarafında `getAddressResponseByIdAndUserId(id, currentUserId)` kullanılıyor; sahiplik yoksa hata.

2. **Güncelleme ve silme sadece sahip için**
   - `updateAddress` ve `deleteAddress` artık `updateAddressForUser` / `deleteAddressForUser` ile çalışıyor; yani "bu adres bu kullanıcıya ait mi?" kontrolü yapılıyor. Başkasının adresini güncelleyemez/silemez.

3. **Tüm adresler sadece admin**
   - `GET /v1/address/find-all` sadece **ADMIN** rolüne açıldı (`@PreAuthorize("hasRole('ADMIN')")`). Normal kullanıcı tüm sistemdeki adresleri göremez.

---

### 3.7. Method Security açıldı

**Ne yaptık?**  
`SecurityConfig` içinde `@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)` kullanıldı.

**Neden?**  
`@PreAuthorize("hasRole('ADMIN')")` gibi anotasyonların çalışması için `prePostEnabled = true` gerekir. Böylece "sadece admin bu metodu çağırabilir" kurallarını controller/service üzerinde tanımlayabiliyoruz.

---

## 4. Özet Tablo (Eski → Yeni)

| Alan      | Eski davranış                          | Yeni davranış                                      |
|----------|-----------------------------------------|----------------------------------------------------|
| Cart     | Path'te `userId`; başkasının sepeti okunabilirdi | userId path'te yok; token'dan alınan kullanıcı kullanılır |
| Order    | Path'te `userId`; adres/ödeme istemciden        | userId token'dan; adres sahipliği + paymentStatus sunucuda |
| Wishlist | Path'te `userId`                         | userId path'te yok; token'dan alınır                |
| Payment  | Path'te `userId`; sipariş sahipliği yok         | userId token'dan; sipariş sahipliği kontrol edilir |
| Address  | Herkes her adresi okuyup güncelleyebilirdi      | Sadece kendi adresi; findAll sadece ADMIN          |
| Order admin | getAllOrders / updateTracking herkese açıktı  | Sadece ADMIN                                        |

---

## 5. Frontend’te yapılması gerekenler

- **Cart:** URL'lerden `userId` kaldırılmalı. Örn: `/v1/cart`, `/v1/cart/items`; isteklerde `Authorization: Bearer <token>` gönderilmeli.
- **Order:** Sipariş oluşturma: `POST /v1/order/save` (path'te userId yok). Müşteri sipariş listesi: `GET /v1/order/my-orders`.
- **Wishlist:** Path'te userId kullanılmamalı; örn. `GET /v1/wishlist`, `POST /v1/wishlist/add/{productId}`.
- **Payment:** `POST /v1/payment/process` çağrısında `userId` parametresi kaldırılmalı; kullanıcı ödemeleri için `GET /v1/payment/my-payments` kullanılmalı.

Tüm bu endpoint'lerde **JWT token** gönderilmesi zorunludur; aksi halde 401 alınır.

---

## 6. Test tarafı

- **SecurityTestUtils:** Unit testlerde "giriş yapmış kullanıcı" simüle etmek için `SecurityTestUtils.setCurrentUser()` / `clearContext()` kullanıldı. Böylece controller'lar `SecurityUtils.getCurrentUserId()` çağırdığında testte bir userId dönüyor.
- Cart, Order, Wishlist, Payment, Address controller testleri yeni path ve parametrelere göre güncellendi.
- Order ve Address servis testlerinde `getAddressById` yerine `getAddressByIdAndUserId` mock'landı.
- `PaymentStatus` enum'una `PENDING` eklendi; ilgili enum testi de güncellendi.

---

Bu dokümandaki değişikliklerin amacı: **kimlik doğrulama (token) ile yetkilendirmeyi (bu kullanıcı bu veriye erişebilir mi?) birleştirip** IDOR ve yetkisiz erişim risklerini azaltmaktır. Path'teki ID'lere güvenmek yerine, "şu an giriş yapan kullanıcı" bilgisini kullanıyoruz.

---

## 7. Sırada ne var? (Öncelik sırasıyla)

Tam bir e-ticaret backend’i ve daha güvenli/ölçeklenebilir bir sistem için sırada yapılabilecekler:

| Öncelik | Konu | Ne yapılır | Neden |
|--------|------|------------|--------|
| **1** | **Rate limiting** | Login, register, ödeme gibi endpoint’lere istek sınırı (örn. dakikada 5 login). Bucket4j veya Spring’in basit bir filter’ı. | Brute force ve kötüye kullanımı azaltır. |
| **2** | **CORS** | Production’da `application.properties` veya bir `WebMvcConfigurer` ile sadece frontend domain’ine izin ver. | Sadece izin verdiğin origin’ler API’yi çağırabilir. |
| **3** | **Stok race condition** | `Product` entity’ye `@Version` ekleyip optimistic locking kullan veya sipariş/satır seviyesinde kısa süreli lock. | Aynı üründen aynı anda çok siparişte yanlış stok düşmesini önler. |
| **4** | **Sipariş listesi sayfalama** | `getAllOrders` ve `getOrdersByUser` için `Pageable` / `Page<OrderResponse>` dön. | Liste büyüdükçe performans ve bellek için gerekli. |
| **5** | **Gerçek ödeme entegrasyonu** | Mock yerine iyzico, Stripe, PayTR vb. bir gateway + (mümkünse) webhook ile ödeme sonucunu güncelle. | Canlı ödeme almak için şart. |
| **6** | **İade (return) akışı** | İade talebi, onay, kısmi iade için model ve endpoint’ler (istersen ayrı dokümanda tasarla). | Müşteri iade sürecini yönetmek için. |
| **7** | **Fatura / fiş** | Sipariş için fatura oluşturma, PDF indirme veya saklama. | Muhasebe ve yasal gereklilik. |
| **8** | **Config güvenliği** | `JWT_SECRET` ve DB şifresini env’den al; `application.properties`’te default/placeholder kalsın, production’da asla gerçek değer yazma. | Secret’ların kodda görünmemesi. |
| **9** | **AuthIntegrationTest** | Test profilde e-posta servisini mock’la (zaten `MockEmailNotificationService` var; test profile’da onu kullan). | CI’da yeşil test, gerçek e-posta API’ye bağımlılık kalkar. |

İlk iki madde (rate limiting + CORS) güvenlik ve stabilite için hızlı kazanım; 3–4 performans ve veri tutarlılığı; 5–7 tam e-ticaret özelliği; 8–9 config ve test kalitesi için.

---

## 8. Destek Talepleri (Support Tickets)

Müşteri destek talepleri için ayrı bir modül eklendi. Tüm endpoint'ler `/v1/support` altında ve **authenticated**; kullanıcı sadece kendi taleplerini görür, admin tüm talepleri görür ve yanıtlayabilir.

| Metot | Endpoint | Kim | Açıklama |
|-------|----------|-----|----------|
| POST | `/v1/support` | Kullanıcı | Yeni destek talebi açar (subject, message). |
| GET | `/v1/support/my-tickets` | Kullanıcı | Kendi taleplerinin listesi. |
| GET | `/v1/support/my-tickets/{ticketId}` | Kullanıcı | Kendi talebini detaylı görür (sahiplık kontrolü var). |
| GET | `/v1/support/all` | ADMIN | Tüm destek talepleri (admin paneli). |
| PUT | `/v1/support/{ticketId}/reply` | ADMIN | Talebe yanıt (adminReply) ve durum (status) günceller. |

**Durumlar (TicketStatus):** `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`.  
**Request örneği (talep açma):** `{ "subject": "Sipariş gecikmesi", "message": "..." }`.  
**Yanıt (admin):** `{ "status": "RESOLVED", "adminReply": "İncelendi, kargo yola çıktı." }`.
