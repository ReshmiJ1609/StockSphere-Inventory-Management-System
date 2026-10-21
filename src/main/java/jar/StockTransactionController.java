
package jar;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockTransactionController {

    private final ProductRepository productRepository;

    public StockTransactionController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostMapping("/issue")
    @Transactional
    public ResponseEntity<?> issueStock(
            @RequestBody Map<String, Object> request) {

        Long productId = Long.valueOf(
                request.get("productId").toString());

        int quantity = Integer.parseInt(
                request.get("quantity").toString());

        if (quantity <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Quantity must be greater than zero"));
        }

        Product product = productRepository.findById(productId)
                .orElse(null);

        if (product == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Product not found"));
        }

        if (product.getQuantity() < quantity) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Insufficient stock"));
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        return ResponseEntity.ok(Map.of(
                "message", "Stock reduced successfully",
                "product", product.getName(),
                "remainingStock", product.getQuantity()
        ));
    }
}
