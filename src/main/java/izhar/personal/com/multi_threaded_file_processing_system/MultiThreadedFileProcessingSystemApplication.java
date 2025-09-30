package izhar.personal.com.multi_threaded_file_processing_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MultiThreadedFileProcessingSystemApplication {

  public static void main(String[] args) {
    SpringApplication.run(MultiThreadedFileProcessingSystemApplication.class, args);
  }
}
