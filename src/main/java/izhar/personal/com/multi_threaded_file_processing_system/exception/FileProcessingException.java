package izhar.personal.com.multi_threaded_file_processing_system.exception;

import lombok.Getter;

import java.sql.Time;

@Getter
public class FileProcessingException extends RuntimeException{
    private String fileName;
    private String operation;
    private final long timestamp;
         public FileProcessingException(String fileName, String operation, String message) {
        super(String.format("Failed to %s file '%s': %s", operation, fileName, message));
        this.fileName = fileName;
        this.operation = operation;
        this.timestamp = System.currentTimeMillis();
    }

    public FileProcessingException(String fileName, String operation, String message, Throwable cause) {
        super(String.format("Failed to %s file '%s': %s", operation, fileName, message), cause);
        this.fileName = fileName;
        this.operation = operation;
        this.timestamp = System.currentTimeMillis();
    }


}
