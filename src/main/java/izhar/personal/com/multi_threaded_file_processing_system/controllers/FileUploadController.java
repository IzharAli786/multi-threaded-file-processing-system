package izhar.personal.com.multi_threaded_file_processing_system.controllers;
//import izhar.personal.com.multi_threaded_file_processing_system.concurrency.ThreadedFileProcessor;
import izhar.personal.com.multi_threaded_file_processing_system.config.CustomAsyncExceptionHandler;
import izhar.personal.com.multi_threaded_file_processing_system.dto.ProcessingResult;
import izhar.personal.com.multi_threaded_file_processing_system.exception.GlobalExceptionHandler;
import izhar.personal.com.multi_threaded_file_processing_system.service.LogService;
import izhar.personal.com.multi_threaded_file_processing_system.service.PdfToWordConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import izhar.personal.com.multi_threaded_file_processing_system.service.ResilientFileProcessingService;
@RestController
@RequestMapping("/users")
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
    private ResilientFileProcessingService resilientFileProcessingService;

    @PostMapping("/upload/files" )
    public ResponseEntity<ByteArrayResource> uploadFiles(@RequestParam("file") MultipartFile[] file) {
        try {
            List<File> convertedFiles = new ArrayList<>();
            for (MultipartFile fileItem : file) {
                File javaFile = toJavaFile(fileItem);
                // 2) convert PDF → Word
                CompletableFuture<ProcessingResult> future = resilientFileProcessingService.processFileResilient(javaFile);
                ProcessingResult result = future.get(30, TimeUnit.SECONDS);

                // 3) read all bytes of the .docx
                if (result.isSuccess()) {
                    System.out.println("the result is Success");
                    String outputPath = javaFile.getAbsolutePath().replaceAll(".pdf$", ".docx");
                    File wordFile = new File(outputPath);
                    convertedFiles.add(wordFile);
                } else {
                    System.out.println("the result is Failed");
                    return ResponseEntity.internalServerError().build();
                }
            }
                if(convertedFiles.size() ==1){
                    File singleFile=convertedFiles.get(0);
                    byte[]data= Files.readAllBytes(singleFile.toPath());
                    ByteArrayResource resource = new ByteArrayResource(data);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + singleFile.getName() + "\"")
                            .contentLength(data.length)
                            .body(resource);
                }
                else{
                    byte[] zipData= createZipFile(convertedFiles);
                    ByteArrayResource resource = new ByteArrayResource(zipData);
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"converted_documents.zip\"")
                            .contentLength(zipData.length)
                            .body(resource);
                }
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
            exceptionHandler.handleGenericException(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[]createZipFile(List<File> files) throws IOException {
        try(ByteArrayOutputStream baos= new ByteArrayOutputStream();
            ZipOutputStream zos= new  ZipOutputStream(baos)){
            for(File file : files){
                ZipEntry entry = new ZipEntry(file.getName());
                zos.putNextEntry(entry);
                byte[] fileBytes=Files.readAllBytes(file.toPath());
                zos.write(fileBytes);
                zos.closeEntry();

            }
            zos.finish();;
            return baos.toByteArray();
        }
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

