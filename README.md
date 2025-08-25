# E-Commerce Backend

Bu proje, modern bir **e-ticaret altyapısı** oluşturmak için geliştirilmiş **Spring Boot tabanlı bir backend uygulamasıdır**.  
Amaç; kullanıcıların kayıt/giriş işlemleri yapabilmesi, ürünleri görüntüleyip sepetlerine ekleyebilmesi ve sipariş oluşturabilmesini sağlamaktır.  
Ayrıca yönetici rolleri sayesinde ürün ve kategori yönetimi de yapılabilmektedir.  

Proje, **katmanlı mimari**, **DTO-Entity dönüşümleri**, **JWT tabanlı kimlik doğrulama**, **MongoDB/Redis entegrasyonu** ve **global exception handling** gibi modern yazılım geliştirme prensiplerini barındırır.  

---

## Kullanılan Teknolojiler

- **Java 17+**
- **Spring Boot** (Web, Security, Validation, Data)
- **MongoDB** → Ana veritabanı
- **Redis** → Cache yönetimi (oturum/token veya sorgu hızlandırma için)
- **JWT (JSON Web Token)** → Kimlik doğrulama ve güvenlik
- **Spring Security** → Yetkilendirme ve erişim kontrolü
- **MapStruct** → DTO ↔ Entity dönüşümleri
- **Lombok** → Boilerplate kod azaltma
- **Maven** → Proje yönetimi ve bağımlılık
- **Docker (opsiyonel)** → Container tabanlı çalıştırma
- **JUnit / Mockito** → Birim testler için

---

## Mimari Yapı


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


---

## Temel Özellikler

- 🔐 **Kullanıcı Yönetimi**  
  - Kayıt / Giriş  
  - JWT tabanlı kimlik doğrulama  
  - Roller (USER / ADMIN)

- 📦 **Ürün Yönetimi**  
  - Ürün CRUD işlemleri  
  - Kategori yönetimi  
  - Sayfalama & sıralama  

- 🛒 **Sepet Yönetimi**  
  - Sepete ürün ekleme/çıkarma/güncelleme  
  - Kullanıcıya özel sepet saklama  

- 📑 **Sipariş Yönetimi**  
  - Sipariş oluşturma  
  - Sipariş durumu takibi  

- ⚡ **Cache & Performans**  
  - Redis ile cacheleme  
  - Token saklama  

---

## Kurulum

```bash

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

---

## API Endpointleri

### 🔐 Authentication & User
| Method | Endpoint               | Açıklama                       | Rol        |
|--------|------------------------|--------------------------------|------------|
| POST   | `/api/auth/register`   | Yeni kullanıcı kaydı           | PUBLIC     |
| POST   | `/api/auth/login`      | Giriş yap (JWT döner)          | PUBLIC     |
| GET    | `/api/users`           | Kullanıcı listesi (sayfalı)    | ADMIN      |
| GET    | `/api/users/{id}`      | Kullanıcı detayı               | ADMIN      |
| PUT    | `/api/users/{id}`      | Kullanıcı güncelle             | ADMIN      |
| DELETE | `/api/users/{id}`      | Kullanıcı sil                  | ADMIN      |

---

### 📦 Products & Categories
| Method | Endpoint                       | Açıklama                        | Rol        |
|--------|--------------------------------|---------------------------------|------------|
| GET    | `/api/products`                | Tüm ürünleri listele (sayfalı, sıralama destekli) | PUBLIC |
| GET    | `/api/products/{id}`           | Ürün detayını getir             | PUBLIC     |
| POST   | `/api/products`                | Yeni ürün ekle                  | ADMIN      |
| PUT    | `/api/products/{id}`           | Ürün güncelle                   | ADMIN      |
| DELETE | `/api/products/{id}`           | Ürün sil                        | ADMIN      |
| GET    | `/api/categories`              | Kategori listesi                 | PUBLIC     |
| POST   | `/api/categories`              | Yeni kategori ekle               | ADMIN      |

---

### 🛒 Cart
| Method | Endpoint                       | Açıklama                      | Rol   |
|--------|--------------------------------|-------------------------------|-------|
| GET    | `/api/cart`                    | Kullanıcının sepetini getir   | USER  |
| POST   | `/api/cart/items`              | Sepete ürün ekle              | USER  |
| PUT    | `/api/cart/items/{itemId}`     | Sepet ürününü güncelle        | USER  |
| DELETE | `/api/cart/items/{itemId}`     | Sepetten ürün çıkar           | USER  |
| DELETE | `/api/cart/clear`              | Sepeti tamamen temizle        | USER  |

---

### 📑 Orders
| Method | Endpoint               | Açıklama                       | Rol   |
|--------|------------------------|--------------------------------|-------|
| POST   | `/api/orders`          | Yeni sipariş oluştur           | USER  |
| GET    | `/api/orders`          | Kullanıcının siparişlerini getir | USER |
| GET    | `/api/orders/{id}`     | Sipariş detayını getir         | USER  |
| PUT    | `/api/orders/{id}/status` | Sipariş durumunu güncelle    | ADMIN |

---

### 📍 Address
| Method | Endpoint                 | Açıklama                | Rol   |
|--------|--------------------------|-------------------------|-------|
| GET    | `/api/addresses`         | Kullanıcının adreslerini getir | USER |
| POST   | `/api/addresses`         | Yeni adres ekle         | USER  |
| PUT    | `/api/addresses/{id}`    | Adres güncelle          | USER  |
| DELETE | `/api/addresses/{id}`    | Adres sil               | USER  |

---

### ⚡ Health & System
| Method | Endpoint             | Açıklama                | Rol   |
|--------|----------------------|-------------------------|-------|
| GET    | `/actuator/health`   | Servis durumu           | PUBLIC|
| GET    | `/actuator/info`     | Build ve proje bilgisi  | ADMIN |

---


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
