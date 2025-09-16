package izhar.personal.com.multi_threaded_file_processing_system.exception;

public class FileValidationException extends FileProcessingException {
  private String validationType;

  public FileValidationException(String fileName, String validationType, String message) {
    super(fileName, "Validate", message);
    this.validationType = validationType;

  }

  public String getValidationType() {
    return validationType;
  }

}
