package izhar.personal.com.multi_threaded_file_processing_system.health;

import izhar.personal.com.multi_threaded_file_processing_system.concurrency.ThreadedFileProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class FileProcessingHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up().build();
    }
}
