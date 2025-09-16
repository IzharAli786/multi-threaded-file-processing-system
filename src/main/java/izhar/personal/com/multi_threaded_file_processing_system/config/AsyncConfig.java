package izhar.personal.com.multi_threaded_file_processing_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Configuration
@EnableAsync
public class AsyncConfig {
  @Bean(name = "TaskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(10);
    executor.setThreadNamePrefix("TaskExecutor-Async");
    executor.initialize();
    return executor;
  }

  @Bean(name = "FileValidator")
  public Executor fileValidator() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(10);
    executor.setThreadNamePrefix("FileValidator-Async");
    executor.initialize();
    return executor;
  }
}
