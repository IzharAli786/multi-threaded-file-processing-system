package izhar.personal.com.multi_threaded_file_processing_system.controllers;
import izhar.personal.com.multi_threaded_file_processing_system.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/logs")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping("/recent")
    public ResponseEntity<List<LogService.LogEntry>> getRecentLogs() {
        return ResponseEntity.ok(logService.getRecentLogs());
    }

    @GetMapping("/file")
    public ResponseEntity<List<String>> getLogFile() {
        return ResponseEntity.ok(logService.getLogFile());
    }

    @GetMapping("/recent/{level}")
    public ResponseEntity<List<LogService.LogEntry>> getLogsByLevel(@PathVariable String level) {
        List<LogService.LogEntry> filteredLogs = logService.getRecentLogs().stream()
                .filter(log -> log.getLevel().equalsIgnoreCase(level))
                .toList();
        return ResponseEntity.ok(filteredLogs);
    }
}