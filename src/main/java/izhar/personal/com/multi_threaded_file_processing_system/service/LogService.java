package izhar.personal.com.multi_threaded_file_processing_system.service;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class LogService {
    private final Queue<LogEntry> recentLogs = new ConcurrentLinkedQueue<>();
    private final int MAX_RECENT_LOGS = 1000;

    public void addLog(String level, String message, String className) {
        LogEntry entry = new LogEntry(
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            level,
            message,
            className
        );

        recentLogs.offer(entry);
        if (recentLogs.size() > MAX_RECENT_LOGS) {
            recentLogs.poll();
        }
    }

    public List<LogEntry> getRecentLogs() {
        return new ArrayList<>(recentLogs);
    }

    public List<String> getLogFile() {
        try {
            Path logPath = Paths.get("logs/application.log");
            if (Files.exists(logPath)) {
                return Files.readAllLines(logPath);
            }
        } catch (IOException e) {
            return Arrays.asList("Error reading log file: " + e.getMessage());
        }
        return Arrays.asList("Log file not found");
    }

    public static class LogEntry {
        private String timestamp;
        private String level;
        private String message;
        private String className;

        public LogEntry(String timestamp, String level, String message, String className) {
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
            this.className = className;
        }

        // Getters
        public String getTimestamp() { return timestamp; }
        public String getLevel() { return level; }
        public String getMessage() { return message; }
        public String getClassName() { return className; }
    }

    @Service
    public static class AsyncServices {
    }
}
