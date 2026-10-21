
package jar.controller;

import jar.Product;
import jar.ProductRepository;
import jar.Supplier;
import jar.SupplierRepository;
import jar.SupplierPurchase;
import jar.SupplierPurchaseRepository;
import jar.StockHistory;
import jar.StockHistoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supplier-purchases")
@CrossOrigin(origins = "*")
public class SupplierPurchaseController {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierPurchaseRepository purchaseRepository;
    private final StockHistoryRepository historyRepository;

    public SupplierPurchaseController(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            SupplierPurchaseRepository purchaseRepository,
            StockHistoryRepository historyRepository) {

        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseRepository = purchaseRepository;
        this.historyRepository = historyRepository;
    }

    @GetMapping
    public List<SupplierPurchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createPurchase(
            @RequestBody Map<String, Object> data) {

        try {
            Long supplierId = Long.valueOf(
                    data.get("supplierId").toString());

            Long productId = Long.valueOf(
                    data.get("productId").toString());

            int purchaseQuantity = Integer.parseInt(
                    data.get("purchaseQuantity").toString());

            LocalDate purchaseDate = data.get("purchaseDate") != null
                    ? LocalDate.parse(data.get("purchaseDate").toString())
                    : LocalDate.now();

            if (purchaseQuantity <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message",
                                "Purchase quantity must be greater than zero."));
            }

            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElse(null);

            if (supplier == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Supplier not found."));
            }

            Product product = productRepository.findById(productId)
                    .orElse(null);

            if (product == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Product not found."));
            }

            int previousStock = product.getQuantity();

            if (purchaseQuantity > previousStock) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message",
                                "Purchase quantity exceeds available stock. Available stock: "
                                        + previousStock));
            }

            int remainingStock = previousStock - purchaseQuantity;

            // Reduce product stock
            product.setQuantity(remainingStock);
            productRepository.save(product);

            // Save supplier purchase record
            SupplierPurchase purchase = new SupplierPurchase();
            purchase.setSupplierId(supplier.getId());
            purchase.setSupplierName(supplier.getName());
            purchase.setProductId(product.getId());
            purchase.setProductName(product.getName());
            purchase.setPurchaseQuantity(purchaseQuantity);
            purchase.setPreviousStock(previousStock);
            purchase.setRemainingStock(remainingStock);
            purchase.setPurchaseDate(purchaseDate);

            SupplierPurchase savedPurchase =
                    purchaseRepository.save(purchase);

            // Save stock history
            StockHistory history = new StockHistory();
            history.setProductId(product.getId());
            history.setProductName(product.getName());
            history.setAction("Supplier Purchase - " + supplier.getName());
            history.setPreviousQuantity(previousStock);
            history.setNewQuantity(remainingStock);
            history.setActionDate(
                    purchaseDate.atStartOfDay());

            historyRepository.save(history);

            return ResponseEntity.ok(savedPurchase);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message",
                            "Purchase failed: " + e.getMessage()));
        }
    }
}
