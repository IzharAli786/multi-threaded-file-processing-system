package izhar.personal.com.multi_threaded_file_processing_system.controllers;
//import izhar.personal.com.multi_threaded_file_processing_system.concurrency.ThreadedFileProcessor;

import izhar.personal.com.multi_threaded_file_processing_system.config.CustomAsyncExceptionHandler;
import izhar.personal.com.multi_threaded_file_processing_system.dto.ProcessingResult;
import izhar.personal.com.multi_threaded_file_processing_system.entity.Job;
import izhar.personal.com.multi_threaded_file_processing_system.entity.ProcessingLogs;
import izhar.personal.com.multi_threaded_file_processing_system.enums.Status;
import izhar.personal.com.multi_threaded_file_processing_system.exception.GlobalExceptionHandler;
import izhar.personal.com.multi_threaded_file_processing_system.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/customer")
public class FileUploadController {
  private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
  private final PdfToWordConverter pdfToWordConverter = new PdfToWordConverter();

  @Autowired
  private CustomAsyncExceptionHandler customAsyncExceptionHandler;
  @Autowired
  private GlobalExceptionHandler exceptionHandler;
  @Autowired
  private LogService logService;
  @Autowired
  private JobService jobService;

  @Autowired
  private ResilientFileProcessingService resilientFileProcessingService;
  private ProcessingLogService processingLogService;
  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  @Autowired
  private StompController stompController;


  @PostMapping("/upload/files")
  public ResponseEntity<InputStreamResource> uploadFiles(@RequestParam("file") MultipartFile[] file, InputStream inputStream) throws Error {
    try {

      Job job = jobService.createJob("myJob1", Status.QUEUED);
      simpMessagingTemplate.convertAndSend("/topic/log", job.getId());
      List<File> convertedFiles = new ArrayList<>();
      for (MultipartFile fileItem : file) {

        if (fileItem.isEmpty()) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        }
        ProcessingLogs processingLogs = new ProcessingLogs(fileItem.getName());
        processingLogs.setFileName(fileItem.getOriginalFilename());
        job.addLog(processingLogs);
        processingLogs.setStatus(Status.QUEUED);
        stompController.notifyJobStatusChange(job.getId(), String.valueOf(job.getStatus()));
        File javaFile = toJavaFile(fileItem);

        CompletableFuture<ProcessingResult> future = resilientFileProcessingService.processFileResilient(javaFile, job, processingLogs);

        ProcessingResult result = future.get(5, TimeUnit.MINUTES);


        if (result.isSuccess()) {
          processingLogs.setStatus(Status.COMPLETED);
          processingLogs.setCompletedAt(LocalDateTime.now());

          String outputPath = javaFile.getAbsolutePath().replaceAll(".pdf$", ".docx");
          File wordFile = new File(outputPath);
          convertedFiles.add(wordFile);

        } else {

          return ResponseEntity.internalServerError().build();
        }
      }

      job.setStatus(Status.COMPLETED);
      stompController.notifyJobStatusChange(job.getId(), String.valueOf(job.getStatus()));

      job.setCompletedAt(LocalDateTime.now());

      logger.info("the job is {}", job);
      if (convertedFiles.size() == 1) {
        File singleFile = convertedFiles.getFirst();
//        byte[] data = Files.readAllBytes(singleFile.toPath());||
        FileInputStream fileInputStream = new FileInputStream(singleFile);
        InputStreamResource resource = new InputStreamResource(fileInputStream);
        return ResponseEntity.ok()
              .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
              .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + singleFile.getName() + "\"")
              .contentLength(singleFile.length())
              .body(resource);
      } else {
        InputStreamResource resource = createZipFile(convertedFiles);

        return ResponseEntity.ok()
              .contentType(MediaType.APPLICATION_OCTET_STREAM)
              .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"converted_documents.zip\"")
              .body(resource);
      }
    } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
      exceptionHandler.handleGenericException(e);
      return ResponseEntity.internalServerError().build();
    }
  }

  private InputStreamResource createZipFile(List<File> files) throws IOException {
    // Step 1: Create a place to store zip data IN MEMORY
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // Step 2: Create ZipOutputStream to WRITE TO memory
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {

      // Step 3: For each file, add it to zip
      for (File file : files) {
        // Create zip entry
        ZipEntry entry = new ZipEntry(file.getName());
        zos.putNextEntry(entry);

        // Read the file and write to zip
        try (FileInputStream fis = new FileInputStream(file)) {
          byte[] buffer = new byte[1024];
          int length;
          while ((length = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
          }
        }

        zos.closeEntry();
      }

      zos.finish();
    }

    // Step 4: Convert memory data to InputStream for response
    byte[] zipBytes = baos.toByteArray();
    return new InputStreamResource(new ByteArrayInputStream(zipBytes));
  }

  @GetMapping("/test-logging")
  public ResponseEntity<String> testLogging() {
    logService.addLog("ERROR", "hello i am in logAddService", this.getClass().toString());

    System.out.println("System.out.println - this should always show in console");

    return ResponseEntity.ok("Check console for log messages");
  }

  private File toJavaFile(MultipartFile multipart) throws IOException {
    String tmpBase = System.getProperty("java.io.tmpdir");
    File tempFolder = new File(tmpBase, "myAppUploads");
    if (!tempFolder.exists() && !tempFolder.mkdirs()) {
      throw new IOException("Could not create temp directory: " + tempFolder);
    }
    String original = multipart.getOriginalFilename();
    File convFile = new File(tempFolder, (original != null ? original : "upload.tmp"));
    try {
      multipart.transferTo(convFile);
    } catch (IOException | IllegalStateException e) {
      exceptionHandler.handleGenericException(e);
      throw new RuntimeException(e);
    }

    return convFile;
  }
}



