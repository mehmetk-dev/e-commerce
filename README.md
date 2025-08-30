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
    |   ├─ enums/            # Projeyle alakalı bütün enum'lar
    │   ├─ exception/        # Özel exception’lar
    │   ├─ handler/          # Global handler
    │   ├─ mapper/           # MapStruct mapper arayüzleri
    │   ├─ model/            # Entity/Document sınıfları
    │   ├─ repository/       # Spring Data repo arayüzleri
    │   ├─ service/          # Servis arayüzleri
    │   └─ service/impl/     # Servis implementasyonları
    │   ├─ util/             # Custom response classları ve global mesaj classı
    └─ resources/
        ├─ application.properties
        └─ ...
```


---

## Temel Özellikler

-  **Kullanıcı Yönetimi**  
  - Kayıt / Giriş  
  - JWT tabanlı kimlik doğrulama  
  - Roller (USER / ADMIN)

-  **Ürün Yönetimi**  
  - Ürün CRUD işlemleri  
  - Kategori yönetimi  
  - Sayfalama & sıralama  

-  **Sepet Yönetimi**  
  - Sepete ürün ekleme/çıkarma/güncelleme  
  - Kullanıcıya özel sepet saklama  

-  **Sipariş Yönetimi**  
  - Sipariş oluşturma  
  - Sipariş durumu takibi
    
-  **Cache & Performans**  
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
`src/main/resources/application.properties` içerisinde gerekli ayarları yap.

**MongoDB örneği**
```
spring.data.mongodb.uri=mongodb://localhost:27017/ecommerce
spring.data.mongodb.database=ecommerce

jwt.secret=buraya-uzun-ve-guclu-bir-secret-girin
jwt.expiration=3600000

```

**PostgreSQL örneği**
```
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
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

###  Authentication & User
| Method | Endpoint              | Açıklama                       | Rol        |
|--------|-----------------------|--------------------------------|------------|
| POST   | `/v1/auth/register`   | Yeni kullanıcı kaydı           | PUBLIC     |
| POST   | `/v1/auth/login`      | Giriş yap (JWT döner)          | PUBLIC     |
| GET    | `/v1/users`           | Kullanıcı listesi (sayfalı)    | ADMIN      |
| GET    | `/v1/users/{id}`      | Kullanıcı detayı               | ADMIN      |
| GET    | `/v1/users/find-all`  | Bütün kullanıcıları listele    | ADMIN      |
| PUT    | `/v1/users/{id}`      | Kullanıcı güncelle             | ADMIN      |
| DELETE | `/v1/users/{id}`      | Kullanıcı sil                  | ADMIN      |

---

###  Products & Categories
| Method | Endpoint                      | Açıklama                        | Rol        |
|--------|-------------------------------|---------------------------------|------------|
| GET    | `/v1/products`                | Tüm ürünleri listele (sayfalı, sıralama destekli)| PUBLIC |
| GET    | `/v1/products/{id}`           | Ürün detayını getir             | PUBLIC     |
| POST   | `/v1/products`                | Yeni ürün ekle                  | ADMIN      |
| PUT    | `/v1/products/{id}`           | Ürün güncelle                   | ADMIN      |
| DELETE | `/v1/products/{id}`           | Ürün sil                        | ADMIN      |
| GET | `/v1/products/search/title`           | Ürün sil                        | ADMIN      |
| GET | `/v1/products/search/category`           | Ürün sil                        | ADMIN      |
| GET    | `/v1/categories`              | Kategori listesi                 | PUBLIC     |
| POST   | `/v1/categories`              | Yeni kategori ekle               | ADMIN      |

---

###  Cart
| Method | Endpoint                      | Açıklama                      | Rol   |
|--------|-------------------------------|-------------------------------|-------|
| GET    | `/v1/cart/{userId}`                    | Kullanıcının sepetini getir   | USER  |
| POST   | `/v1/cart/{userId}/save`              | Sepeti kaydet           | USER  |
| POST   | `/v1/cart/{userId}/items`              | Sepete ürün ekle              | USER  |
| PUT    | `/{userId}/items/{productId}`     | Ürün stoğu güncelle        | USER  |
| DELETE | `/v1/cart/items/{itemId}`     | Sepetten ürün çıkar           | USER  |
| DELETE | `/v1/cart/clear`              | Sepeti tamamen temizle        | USER  |
| GET | `/v1/cart/{userId}/total`              | Sepet toplam tutarı görüntüle       | USER  |

---

###  Orders
| Method | Endpoint                | Açıklama                       | Rol   |
|--------|-------------------------|--------------------------------|-------|
| POST   | `/v1/order/save`            | Yeni sipariş oluştur           | USER  |
| GET    | `/v1/order/user/{userId}`            | Kullanıcının siparişlerini getir | USER |
| GET    | `/v1/order/{id}`       | Sipariş detayını getir         | USER  |
| DELETE    | `/v1/order/{orderId}`       | Siparişi sil         | USER  |
| GET    | `/v1/order`| Tüm siparişleri listele (sayfalı, sıralama destekli)    | ADMIN |

---

###  Address
| Method | Endpoint                 | Açıklama                | Rol   |
|--------|--------------------------|-------------------------|-------|
| GET    | `/v1/address/{id}`         | ID'ye göre adres getir | USER |
| GET    | `/v1/address/find-all`  | Bütün adresleri listele    | ADMIN      |
| POST   | `/v1/address/save`         | Yeni adres ekle         | USER  |
| PUT    | `/v1/address/{id}`    | Adres güncelle          | USER  |
| DELETE | `/v1/address/{id}`    | Adres sil               | USER  |

---

###  Review
| Method | Endpoint                 | Açıklama                | Rol   |
|--------|--------------------------|-------------------------|-------|
| GET    | `/v1/review/{id}`         | ID'ye göre yorum getir | USER |
| GET    | `/v1/review/find-all`  | Bütün yorumları listele    | ADMIN      |
| POST   | `/v1/review/save`         | Yeni yorum ekle         | USER  |
| PUT    | `/v1/review/{id}`    | Yorum güncelle          | USER  |
| DELETE | `/v1/review/{id}`    | Yorum sil               | USER  |

---

###  Payment
| Method | Endpoint                 | Açıklama                | Rol   |
|--------|--------------------------|-------------------------|-------|
| GET    | `/v1/payment/{paymentId}`         | ID'ye göre ödeme getir | USER |
| GET    | `/v1/payment/user/{userId}`  | Kullanıcının ödemelerini getir    | ADMIN      |
| POST   | `/v1/payment/process`         | Yeni ödeme ekle         | USER  |
| PUT    | `/v1/payment/{paymentId}/status`    | Ödeme durumunu güncelle          | USER  |
| DELETE | `/v1/payment/{id}`    | Yorum sil               | USER  |

---

## Geliştirme Notları
- Transaction’lar sadece replica set veya sharded cluster üzerinde çalışır. Ben projede MongoDB Atlas kullandım.
- Listeleme için `PageRequest` ile sayfalama ve sıralama desteklenir.

--

### İncelediğiniz için teşekkür ederim / mehmetkerem2109@gmail.com üzerinden ulaşabilirsiniz.
