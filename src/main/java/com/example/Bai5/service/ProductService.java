package com.example.Bai5.service;

import com.example.Bai5.model.Product;
import com.example.Bai5.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final String UPLOAD_DIR = "uploads/assets/";

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public List<Product> getAll() {
		return productRepository.findAll();
	}

	public Product getById(Long id) {
		return productRepository.findById(id).orElse(null);
	}

	public Product save(Product product) {
		return productRepository.save(product);
	}

	public void delete(Long id) {
		productRepository.deleteById(id);
	}

	public String uploadImage(MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			return null;
		}

		// Tạo thư mục nếu chưa có
		Files.createDirectories(Paths.get(UPLOAD_DIR));

		// Lấy phần mở rộng file
		String originalFilename = file.getOriginalFilename();
		String ext = originalFilename.substring(originalFilename.lastIndexOf("."));

		// Tạo tên file duy nhất
		String fileName = UUID.randomUUID() + ext;

		Path path = Paths.get(UPLOAD_DIR + fileName);

		// Copy file vào thư mục
		Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

		return fileName;
	}
}