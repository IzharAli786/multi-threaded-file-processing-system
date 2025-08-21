package izhar.personal.com.multi_threaded_file_processing_system.controllers;

import izhar.personal.com.multi_threaded_file_processing_system.concurrency.ThreadedFileProcessor;
import izhar.personal.com.multi_threaded_file_processing_system.dto.ProcessingResult;
import izhar.personal.com.multi_threaded_file_processing_system.service.PdfToWordConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.*;

@RestController
@RequestMapping("/users")
public class FileUploadController {
    @Autowired
    private ThreadedFileProcessor threadedFileProcessor;
    private final PdfToWordConverter pdfToWordConverter = new PdfToWordConverter();

    @PostMapping("/upload/files")
    public ResponseEntity<ByteArrayResource> uploadFiles(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // 1) turn MultipartFile into a File
            File javaFile = toJavaFile(file);
            // 2) convert PDF → Word
            CompletableFuture<ProcessingResult> future= threadedFileProcessor.processFileAsync(javaFile);
//            File wordFile = pdfToWordConverter.convertToWord(javaFile);
            ProcessingResult result= future.get(30, TimeUnit.SECONDS);
            System.out.println("the result is "+result);
            // 3) read all bytes of the .docx
            if(result.isSuccess()){
                System.out.println("the result is Success");
                String outputPath= javaFile.getAbsolutePath().replaceAll(".pdf$",".docx");
                File wordFile = new File(outputPath);
               byte[] data = Files.readAllBytes(wordFile.toPath());
                ByteArrayResource byteArrayResource= new ByteArrayResource(data);
                // 4) stream it back as a download
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + wordFile.getName() + "\"").header("ThreadName",result.getThreadName())
                        .contentLength(data.length)
                        .body(byteArrayResource);
            } else{
                System.out.println("the result is Failed");
                return ResponseEntity.internalServerError().build();
            }
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {

            return ResponseEntity.internalServerError().build();
        }
    }

    private File toJavaFile(MultipartFile multipart) throws IOException {
        String tmpBase = System.getProperty("java.io.tmpdir");
        File tempFolder = new File(tmpBase, "myAppUploads");
        if (!tempFolder.exists() && !tempFolder.mkdirs()) {
            throw new IOException("Could not create temp directory: " + tempFolder);
        }
        String original = multipart.getOriginalFilename();
        File convFile = new File(tempFolder,
                (original != null ? original : "upload.tmp"));
        multipart.transferTo(convFile);
        return convFile;
    }
}

