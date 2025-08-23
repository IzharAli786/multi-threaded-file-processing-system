package izhar.personal.com.multi_threaded_file_processing_system.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    public String threadName;
    private static  final Logger logger= LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {

        logger.error("Uncaught async exception in method: {}.{}() with parameters: {}",
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                params,
                ex
                );

    }


}
