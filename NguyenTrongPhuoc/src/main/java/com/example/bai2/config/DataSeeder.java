package com.example.bai2.config;

import com.example.bai2.model.Category;
import com.example.bai2.model.PriceEntry;
import com.example.bai2.model.PriceType;
import com.example.bai2.model.Product;
import com.example.bai2.repository.CategoryRepository;
import com.example.bai2.repository.PriceEntryRepository;
import com.example.bai2.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PriceEntryRepository priceEntryRepository;

    public DataSeeder(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            PriceEntryRepository priceEntryRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.priceEntryRepository = priceEntryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0 || productRepository.count() > 0) {
            return;
        }

        Category electronics = new Category();
        electronics.setName("Điện tử");
        electronics.setDescription("Thiết bị điện tử và phụ kiện");
        categoryRepository.save(electronics);

        Category home = new Category();
        home.setName("Gia dụng");
        home.setDescription("Sản phẩm dùng trong gia đình");
        categoryRepository.save(home);

        Product iphone = createProduct("IP15-128-BLK", "iPhone 15 128GB", "Phiên bản màu đen, chính hãng VN/A", electronics);
        Product galaxy = createProduct("SS-S24-256", "Samsung Galaxy S24 256GB", "Màn hình Dynamic AMOLED, màu xám titanium", electronics);
        Product airpods = createProduct("APP2-USB-C", "AirPods Pro 2 USB-C", "Tai nghe chống ồn chủ động, hộp sạc USB-C", electronics);
        Product watch = createProduct("WATCH-S9-45", "Apple Watch Series 9 45mm", "Đồng hồ thông minh GPS, dây sport loop", electronics);
        Product laptop = createProduct("ASUS-VB15", "ASUS Vivobook 15", "Laptop văn phòng 15 inch, SSD 512GB", electronics);

        Product airFryer = createProduct("AIR-FRY-06L", "Nồi chiên không dầu 6L", "Dung tích lớn, công nghệ Rapid Air", home);
        Product purifier = createProduct("AIR-PURE-32", "Máy lọc không khí 32m2", "Lọc bụi mịn PM2.5, cảm biến chất lượng không khí", home);
        Product vacuum = createProduct("ROBOT-VAC-X2", "Robot hút bụi X2", "Điều hướng laser, lau nhà cơ bản", home);
        Product coffee = createProduct("COFFEE-BREW-12", "Máy pha cà phê mini", "Pha nhanh 12 tách, phù hợp gia đình", home);

        createPriceEntry(iphone, PriceType.RETAIL, "21990000", "Giá niêm yết cửa hàng");
        createPriceEntry(iphone, PriceType.ONLINE, "21490000", "Áp dụng kênh online");
        createPriceEntry(galaxy, PriceType.RETAIL, "18990000", "Giá bán lẻ chính hãng");
        createPriceEntry(airpods, PriceType.RETAIL, "5490000", "Giá niêm yết");
        createPriceEntry(watch, PriceType.RETAIL, "10990000", "Giá tiêu chuẩn");
        createPriceEntry(laptop, PriceType.RETAIL, "15990000", "Giá bán lẻ");

        createPriceEntry(airFryer, PriceType.RETAIL, "2890000", "Giá tiêu chuẩn");
        createPriceEntry(purifier, PriceType.RETAIL, "4290000", "Áp dụng tại showroom");
        createPriceEntry(vacuum, PriceType.RETAIL, "6490000", "Giá bán lẻ");
        createPriceEntry(coffee, PriceType.RETAIL, "1690000", "Giá niêm yết");
    }

    private Product createProduct(String sku, String name, String description, Category category) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setActive(true);
        return productRepository.save(product);
    }

    private void createPriceEntry(Product product, PriceType priceType, String price, String note) {
        PriceEntry entry = new PriceEntry();
        entry.setProduct(product);
        entry.setPriceType(priceType);
        entry.setPrice(new BigDecimal(price));
        entry.setCurrency("VND");
        entry.setEffectiveDate(LocalDate.now());
        entry.setNote(note);
        priceEntryRepository.save(entry);
    }
}
