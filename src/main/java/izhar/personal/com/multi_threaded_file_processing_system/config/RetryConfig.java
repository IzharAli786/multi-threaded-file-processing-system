package izhar.personal.com.multi_threaded_file_processing_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.ExponentialBackOff;

import java.io.IOException;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRetry
public class RetryConfig {
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(IOException.class, true);
        retryableExceptions.put(SQLException.class, true);
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(3,retryableExceptions);
        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        ExponentialBackOffPolicy exponentialBackOff = new ExponentialBackOffPolicy();
        exponentialBackOff.setInitialInterval(500);
        exponentialBackOff.setMaxInterval(500);
        exponentialBackOff.setMultiplier(2);
        retryTemplate.setBackOffPolicy( exponentialBackOff);
        retryTemplate.setThrowLastExceptionOnExhausted(true);
        return retryTemplate;
    }
}
