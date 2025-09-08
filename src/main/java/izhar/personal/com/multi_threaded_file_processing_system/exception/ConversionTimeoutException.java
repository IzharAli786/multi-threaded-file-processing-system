package izhar.personal.com.multi_threaded_file_processing_system.exception;

public class ConversionTimeoutException extends FileProcessingException{
        private long timeoutSeconds;
    public ConversionTimeoutException(String fileName, long timeoutSeconds) {
        super(fileName, "Convert", "after timout of "+timeoutSeconds+" seconds" );
        this.timeoutSeconds = timeoutSeconds;
    }
    private long getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
