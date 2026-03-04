package com.mehmetkerem.config;

import com.mehmetkerem.enums.Role;
import com.mehmetkerem.model.Category;
import com.mehmetkerem.model.Product;
import com.mehmetkerem.model.User;
import com.mehmetkerem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final CategoryRepository categoryRepository;
        private final ProductRepository productRepository;
        private final OrderRepository orderRepository;
        private final OrderReturnRepository orderReturnRepository;
        private final ReviewRepository reviewRepository;
        private final WishlistRepository wishlistRepository;
        private final CartRepository cartRepository;
        private final PasswordEncoder passwordEncoder;

        @Value("${app.admin.password:#{null}}")
        private String adminPassword;
        private final SupportTicketRepository supportTicketRepository;

        @Override
        @Transactional
        public void run(String... args) {
                log.info("🏁 Veri başlatma süreci başladı...");
                initializeAdmin();
                initializeCategoriesAndProducts();
                log.info("🏁 Veri başlatma süreci tamamlandı.");
        }

        private void initializeAdmin() {
                if (!userRepository.existsByEmail("admin@canantika.com")) {
                        log.info("Varsayılan Admin kullanıcısı oluşturuluyor...");
                        User admin = User.builder()
                                        .email("admin@canantika.com")
                                        .name("Sistem Yöneticisi")
                                        .passwordHash(passwordEncoder
                                                        .encode(adminPassword != null ? adminPassword : "admin123"))
                                        .role(Role.ADMIN)
                                        .build();
                        userRepository.save(admin);
                        log.info("✅ ADMIN OLUŞTURULDU: Email: admin@canantika.com");
                }
        }

        private void initializeCategoriesAndProducts() {
                if (categoryRepository.count() >= 6 && productRepository.count() >= 20) {
                        log.info("Sistemde zaten yeterli kategori ({}) ve ürün ({}) var. Atlaniyor.",
                                        categoryRepository.count(), productRepository.count());
                        return;
                }

                log.info("📦 Mevcut veriler temizleniyor...");
                try {
                        supportTicketRepository.deleteAll();
                        orderReturnRepository.deleteAll();
                        orderRepository.deleteAll();
                        reviewRepository.deleteAll();
                        wishlistRepository.deleteAll();
                        cartRepository.deleteAll();
                        productRepository.deleteAll();
                        categoryRepository.deleteAll();
                        log.info("🧹 Temizlik başarılı.");
                } catch (Exception e) {
                        log.error("❌ Temizlik sırasında hata: {}", e.getMessage());
                        // Devam etmeye çalış
                }

                log.info("🏷️ Kategoriler oluşturuluyor...");
                Category mobilya = createCategory("Mobilya", "Dönemsel estetiği yansıtan antik mobilyalar.");
                Category aydinlatma = createCategory("Aydınlatma", "Mekana ruh katan aydınlatma parçaları.");
                Category saatler = createCategory("Saatler", "Mekanik harikası antika saatler.");
                Category taki = createCategory("Takı & Aksesuar", "Nadide el yapımı aksesuarlar.");
                Category sanat = createCategory("Sanat & Tablo", "Sanat eserleri ve tablolar.");
                Category kitap = createCategory("Kitap & Efemera", "Nadir kitaplar ve koleksiyonluk belgeler.");

                log.info("💎 Ürünler ekleniyor...");
                // 20 Ürün - Detaylı Özellikler
                createProduct("Louis XVI Marküteri Masa", "18.yy sonu Fransız stili.", 12500.0, 1, mobilya.getId(),
                                "Gül Ağacı, Ceviz", "1800'ler", "Çok İyi");
                createProduct("Antika Chester Berjer", "Hakiki deri İngiliz tarzı.", 8200.0, 2, mobilya.getId(),
                                "Hakiki Deri, Ahşap", "1950'ler", "İyi");
                createProduct("Ahşap El Oyması Sandık", "Tamamen ceviz çeyiz sandığı.", 4500.0, 3, mobilya.getId(),
                                "Ceviz Ağacı", "1920'ler", "Mükemmel");

                createProduct("Art Deco Kristal Avize", "1930'lar Fransız kristal.", 6700.0, 2, aydinlatma.getId(),
                                "Kristal, Pirinç", "1930'lar", "Çalışır Durumda");
                createProduct("Bronz Şamdan Takımı", "19.yy Fransız 5 kollu.", 5400.0, 1, aydinlatma.getId(),
                                "Bronz Döküm",
                                "1890'lar", "Kusursuz");
                createProduct("Opalin Gaz Lambası", "El boyaması çiçek desenli.", 1800.0, 5, aydinlatma.getId(),
                                "Opalin Cam, Pirinç", "1940'lar", "Kullanılabilir");

                createProduct("Osmanlı Savatlı Saat", "Gümüş savatlı cep saati.", 3800.0, 1, saatler.getId(),
                                "Gümüş (900 Ayar)", "Osmanlı Dönemi", "Çalışıyor");
                createProduct("Viktorya Duvar Saati", "Fransız mekanizmalı gotik.", 7200.0, 1, saatler.getId(),
                                "Maun, Bronz",
                                "1880'ler", "Bakımlı");
                createProduct("Pirinç Güneş Saati", "Denizcilik temalı dekoratif.", 2500.0, 4, saatler.getId(),
                                "Pirinç",
                                "1960'lar", "Dekoratif");

                createProduct("Damla Kehribar Tesbih", "Sertifikalı ateş kehribarı.", 4200.0, 2, taki.getId(),
                                "Damla Kehribar",
                                "Yeni", "Sıfır");
                createProduct("Mine İşlemeli Kutu", "Rus gümüşü koleksiyonluk.", 3100.0, 1, taki.getId(), "Gümüş, Mine",
                                "1910'lar", "Koleksiyonluk");
                createProduct("Osmanlı Gümüş Bilezik", "Trabzon telkari el yapımı.", 1950.0, 2, taki.getId(), "Gümüş",
                                "Erken Cumhuriyet", "İyi");

                createProduct("El Yazması Hat Levha", "Sülüs yazı altın varak.", 9500.0, 1, sanat.getId(),
                                "Kağıt, Altın Varak",
                                "19. Yüzyıl", "Çok İyi");
                createProduct("Yağlı Boya Tablo", "1920'ler İstanbul manzarası.", 15000.0, 1, sanat.getId(),
                                "Tuval, Yağlı Boya", "1920'ler", "İyi");
                createProduct("Murano Sanat Camı", "Venedik imzalı figür.", 2800.0, 3, sanat.getId(), "Murano Camı",
                                "1970'ler",
                                "Kusursuz");

                createProduct("Antika Avrupa Haritası", "18.yy bakır baskı gravür.", 4800.0, 1, kitap.getId(), "Kağıt",
                                "1750'ler", "Çerçeveli");
                createProduct("Kütahya Çini Vazo", "Usta imzalı nadide parça.", 3200.0, 3, kitap.getId(),
                                "Seramik, Çini",
                                "1950'ler", "Hatadeız");
                createProduct("Denizci Sekstantı", "Pirinç kutulu navigasyon.", 5900.0, 1, kitap.getId(), "Pirinç, Cam",
                                "1940'lar", "Tam Takım");
                createProduct("Antika Gramofon", "His Master's Voice borulu.", 11000.0, 1, kitap.getId(),
                                "Ahşap, Metal",
                                "1930'lar", "Plak Çalar");
                createProduct("Singer Dikiş Makinesi", "1900 başı pedallı döküm.", 2700.0, 2, mobilya.getId(),
                                "Döküm Demir",
                                "1910'lar", "Dekoratif");

                log.info("✅ 20 ürün 'attribute' verileriyle başarıyla oluşturuldu.");
        }

        private Category createCategory(String name, String description) {
                Category category = Category.builder().name(name).description(description).build();
                return categoryRepository.save(category);
        }

        private void createProduct(String title, String description, double price, int stock, Long categoryId,
                        String material, String era, String condition) {
                java.util.Map<String, Object> attributes = new java.util.HashMap<>();
                attributes.put("material", material);
                attributes.put("era", era);
                attributes.put("condition", condition);

                Product product = Product.builder()
                                .title(title)
                                .description(description)
                                .price(BigDecimal.valueOf(price))
                                .stock(stock)
                                .categoryId(categoryId)
                                .imageUrls(List.of("https://images.unsplash.com/photo-1554034483-04fda0d3507b"))
                                .attributes(attributes)
                                .build();
                productRepository.save(product);
        }
}
