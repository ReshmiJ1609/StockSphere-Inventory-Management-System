
package jar.controller;

import jar.Product;
import jar.ProductRepository;
import jar.StockHistory;
import jar.StockHistoryRepository;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepository;
    private final StockHistoryRepository historyRepository;

    public ProductController(
            ProductRepository productRepository,
            StockHistoryRepository historyRepository) {
        this.productRepository = productRepository;
        this.historyRepository = historyRepository;
    }

    // Save a stock history record
    private void recordHistory(
            Product product,
            String action,
            int previousQuantity,
            int newQuantity) {

        StockHistory history = new StockHistory();
        history.setProductId(product.getId());
        history.setProductName(product.getName());
        history.setAction(action);
        history.setPreviousQuantity(previousQuantity);
        history.setNewQuantity(newQuantity);
        history.setActionDate(LocalDateTime.now());

        historyRepository.save(history);
    }

    // Get all products
    @GetMapping
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    // Add a product
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        Product savedProduct = productRepository.save(product);

        recordHistory(
                savedProduct,
                "PRODUCT ADDED",
                0,
                savedProduct.getQuantity()
        );

        return savedProduct;
    }

    // Update an existing product
    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found: " + id));

        int previousQuantity = product.getQuantity();

        product.setName(updatedProduct.getName());
        product.setCategory(updatedProduct.getCategory());
        product.setQuantity(updatedProduct.getQuantity());
        product.setPrice(updatedProduct.getPrice());
        product.setMinimumStock(updatedProduct.getMinimumStock());

        Product savedProduct = productRepository.save(product);

        recordHistory(
                savedProduct,
                "PRODUCT UPDATED",
                previousQuantity,
                savedProduct.getQuantity()
        );

        return savedProduct;
    }

    // Delete a product
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found: " + id));

        recordHistory(
                product,
                "PRODUCT DELETED",
                product.getQuantity(),
                0
        );

        productRepository.deleteById(id);
    }
}
