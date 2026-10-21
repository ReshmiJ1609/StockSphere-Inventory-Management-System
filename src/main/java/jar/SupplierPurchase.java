
package jar;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "supplier_purchases")
public class SupplierPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long supplierId;
    private String supplierName;
    private Long productId;
    private String productName;
    private Integer purchaseQuantity;
    private Integer previousStock;
    private Integer remainingStock;
    private LocalDate purchaseDate;

    public SupplierPurchase() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getPurchaseQuantity() { return purchaseQuantity; }
    public void setPurchaseQuantity(Integer purchaseQuantity) {
        this.purchaseQuantity = purchaseQuantity;
    }

    public Integer getPreviousStock() { return previousStock; }
    public void setPreviousStock(Integer previousStock) {
        this.previousStock = previousStock;
    }

    public Integer getRemainingStock() { return remainingStock; }
    public void setRemainingStock(Integer remainingStock) {
        this.remainingStock = remainingStock;
    }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}
