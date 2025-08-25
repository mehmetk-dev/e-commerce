# E-Commerce (Spring Boot)

Bu proje; ürün, sepet, sipariş ve kullanıcı yönetimi barındıran, **Spring Boot** tabanlı bir e-ticaret backend’idir. Katmanlı mimari, DTO/Mapper kullanımı ve doğrulama ile temiz bir yapı hedeflenmiştir.  

## İçerik
- [Teknolojiler](#teknolojiler)
- [Mimari ve Klasör Yapısı](#mimari-ve-klasör-yapısı)
- [Özellikler](#özellikler)
- [Kurulum](#kurulum)
- [Yapılandırma](#yapılandırma)
- [Çalıştırma](#çalıştırma)
- [Örnek İstekler (cURL)](#örnek-istekler-curl)
- [Geliştirme Notları](#geliştirme-notları)
- [Yol Haritası](#yol-haritası)
- [Lisans](#lisans)

## Teknolojiler
- **Java 17+**
- **Spring Boot** (Web, Validation, Security)
- **Spring Data** (MongoDB ya da JPA)
- **JWT** tabanlı kimlik doğrulama
- **MapStruct** (DTO ↔ Entity dönüşümleri)
- **Lombok**
- **Maven**

## Mimari ve Klasör Yapısı
```
src/
 └─ main/
    ├─ java/com/…/ecommerce/
    │   ├─ config/           # Güvenlik, CORS vb.
    │   ├─ controller/       # REST controller’lar
    │   ├─ dto/
    │   │   ├─ request/
    │   │   └─ response/
    │   ├─ exception/        # Global handler, özel exception’lar
    │   ├─ mapper/           # MapStruct mapper arayüzleri
    │   ├─ model/            # Entity/Document sınıfları
    │   ├─ repository/       # Spring Data repo arayüzleri
    │   ├─ service/          # Servis arayüzleri
    │   └─ service/impl/     # Servis implementasyonları
    └─ resources/
        ├─ application.yml
        └─ ...
```

## Özellikler
- Kullanıcı kayıt/giriş (JWT ile)
- Ürün CRUD (listeleme, filtreleme, sıralama)
- Sepet yönetimi (ekleme/çıkarma/güncelleme)
- Sipariş oluşturma ve sipariş durum takibi
- DTO & MapStruct ile katman izolasyonu
- Doğrulama ve global hata yönetimi

## Kurulum
```bash
# Klonla
git clone https://github.com/mehmetk-dev/e-commerce.git
cd e-commerce

# Java ve Maven wrapper ile derle
./mvnw clean install
```

## Yapılandırma
`src/main/resources/application.yml` içerisinde gerekli ayarları yap.

**MongoDB örneği**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/ecommerce
      database: ecommerce

jwt:
  secret: "buraya-uzun-ve-guclu-bir-secret-girin"
  expiration: 3600000
```

**PostgreSQL örneği**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## Çalıştırma
```bash
./mvnw spring-boot:run
```
veya
```bash
java -jar target/e-commerce-*.jar
```

## Örnek İstekler (cURL)

**Kayıt**
```bash
curl -X POST http://localhost:8080/api/auth/register   -H "Content-Type: application/json"   -d '{"name":"Mehmet","email":"mehmet@example.com","password":"Sifre123!"}'
```

**Giriş**
```bash
curl -X POST http://localhost:8080/api/auth/login   -H "Content-Type: application/json"   -d '{"email":"mehmet@example.com","password":"Sifre123!"}'
```

**Ürün ekleme**
```bash
curl -X POST http://localhost:8080/api/products   -H "Authorization: Bearer <JWT>"   -H "Content-Type: application/json"   -d '{"name":"Koltuk","price":1999.90,"categoryId":"<id>"}'
```

**Sepete ekleme**
```bash
curl -X POST http://localhost:8080/api/cart/items   -H "Authorization: Bearer <JWT>"   -H "Content-Type: application/json"   -d '{"productId":"<urun-id>","quantity":2}'
```

## Geliştirme Notları
- MapStruct için IDE’de annotation processing aktif olmalı.
- Para alanlarında `BigDecimal` kullanılmalı.
- `@Transactional` gerekli servis metodlarına eklenmeli.
- Listeleme için `PageRequest` ile sayfalama ve sıralama desteklenir.

## Yol Haritası
- [ ] Ürün arama/filtreleme
- [ ] Stok takibi
- [ ] Ödeme sağlayıcı entegrasyonu
- [ ] Docker Compose dosyası
- [ ] CI/CD entegrasyonu

## Lisans
Henüz belirtilmedi. Dilersen **MIT** lisansını ekleyebilirsin.
