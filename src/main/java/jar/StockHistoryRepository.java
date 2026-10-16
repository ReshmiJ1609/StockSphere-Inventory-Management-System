
package jar;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockHistoryRepository
        extends JpaRepository<StockHistory, Long> {

    List<StockHistory> findAllByOrderByActionDateDesc();
}
