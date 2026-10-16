
package jar.controller;

import jar.StockHistory;
import jar.StockHistoryRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stock-history")
@CrossOrigin(origins = "*")
public class StockHistoryController {

    private final StockHistoryRepository historyRepository;

    public StockHistoryController(
            StockHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @GetMapping
    public List<StockHistory> getHistory() {
        return historyRepository.findAllByOrderByActionDateDesc();
    }

    @PostMapping
    public StockHistory addHistory(
            @RequestBody StockHistory history) {

        if (history.getActionDate() == null) {
            history.setActionDate(LocalDateTime.now());
        }

        return historyRepository.save(history);
    }
}
