package izhar.personal.com.multi_threaded_file_processing_system.concurrency;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {
    @Bean
  public ExecutorService fileProcessingExecutorService() {
        ThreadFactory threadFactory= r-> {
                Thread t = new Thread(r);
                t.setName("fileProcessingThread"+t.getId());
                return t;
            };
            return Executors.newFixedThreadPool(5,threadFactory);
        }
    }

