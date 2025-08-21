package izhar.personal.com.multi_threaded_file_processing_system.concurrency;

import lombok.Data;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;



public class ProcessingContext {
    private static final InheritableThreadLocal<LocalDateTime> startTime = new InheritableThreadLocal<>();

    private static final ThreadLocal<File> originalfile  = new ThreadLocal<>();
    public static void clear(){
        startTime.remove();
        originalfile.remove();
    }
    public static void setStartTime(LocalDateTime localDateTime){
        startTime.set(localDateTime);
    }
    public static void setOriginalFile(File file) {
        originalfile.set(file);
    }


    public static File getOriginalFile() {
        return originalfile.get();
    }
    public static LocalDateTime getStartTime(){
        return startTime.get();
    }

}
