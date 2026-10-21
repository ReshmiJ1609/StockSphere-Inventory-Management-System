
package jar.controller;

import jar.Product;
import jar.ProductRepository;
import jar.StockHistory;
import jar.StockHistoryRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

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

    // Record stock history
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

    // Get stock history (latest records first)
    @GetMapping("/history")
    public List<StockHistory> getStockHistory() {

        return historyRepository.findAll(
                Sort.by(
                        Sort.Direction.DESC,
                        "actionDate"
                )
        );
    }

    // Add product
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

    // Update product
    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found: " + id
                        ));

        int previousQuantity = product.getQuantity();

        product.setName(updatedProduct.getName());
        product.setCategory(updatedProduct.getCategory());
        product.setQuantity(updatedProduct.getQuantity());
        product.setPrice(updatedProduct.getPrice());

        Product savedProduct = productRepository.save(product);

        recordHistory(
                savedProduct,
                "PRODUCT UPDATED",
                previousQuantity,
                savedProduct.getQuantity()
        );

        return savedProduct;
    }

    // Delete product
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found: " + id
                        ));

        recordHistory(
                product,
                "PRODUCT DELETED",
                product.getQuantity(),
                0
        );

        productRepository.deleteById(id);
    }
}