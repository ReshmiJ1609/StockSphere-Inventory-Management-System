
package jar.controller;

import jar.Product;
import jar.ProductRepository;
import jar.Supplier;
import jar.SupplierRepository;
import jar.SupplierTransaction;
import jar.SupplierTransactionRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierTransactionRepository transactionRepository;

    public SupplierController(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            SupplierTransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.transactionRepository = transactionRepository;
    }

    // View all suppliers
    @GetMapping
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    // Add supplier
    @PostMapping
    public Supplier addSupplier(@RequestBody Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    // Edit supplier
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(
            @PathVariable Long id,
            @RequestBody Supplier updatedSupplier) {

        return supplierRepository.findById(id)
                .map(supplier -> {
                    supplier.setName(updatedSupplier.getName());
                    supplier.setCompany(updatedSupplier.getCompany());
                    supplier.setPhone(updatedSupplier.getPhone());
                    supplier.setEmail(updatedSupplier.getEmail());
                    supplier.setAddress(updatedSupplier.getAddress());
                    supplier.setProductSupplied(
                            updatedSupplier.getProductSupplied());

                    return ResponseEntity.ok(
                            supplierRepository.save(supplier));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete supplier
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id) {
        if (!supplierRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        supplierRepository.deleteById(id);
        return ResponseEntity.ok(
                Map.of("message", "Supplier deleted successfully"));
    }

    // Issue stock to supplier
    @PostMapping("/issue-stock")
    @Transactional
    public ResponseEntity<?> issueStock(
            @RequestBody Map<String, Object> data) {

        Long supplierId =
                Long.valueOf(data.get("supplierId").toString());
        Long productId =
                Long.valueOf(data.get("productId").toString());
        int quantity =
                Integer.parseInt(data.get("quantity").toString());

        if (quantity <= 0) {
            return ResponseEntity.badRequest()
                    .body("Quantity must be greater than zero");
        }

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() ->
                        new RuntimeException("Supplier not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        if (product.getQuantity() < quantity) {
            return ResponseEntity.badRequest()
                    .body("Insufficient stock");
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        SupplierTransaction transaction = new SupplierTransaction();
        transaction.setSupplierId(supplier.getId());
        transaction.setProductId(product.getId());
        transaction.setQuantity(quantity);
        transaction.setTransactionDate(LocalDateTime.now());

        transactionRepository.save(transaction);

        return ResponseEntity.ok(Map.of(
                "message", "Stock issued successfully",
                "product", product.getName(),
                "remainingStock", product.getQuantity()
        ));
    }
}
