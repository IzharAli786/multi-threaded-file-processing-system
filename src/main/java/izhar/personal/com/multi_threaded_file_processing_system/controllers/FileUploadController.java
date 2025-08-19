package izhar.personal.com.multi_threaded_file_processing_system.controllers;

import izhar.personal.com.multi_threaded_file_processing_system.service.PdfToWordConverter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/users")
public class FileUploadController {

    private final PdfToWordConverter pdfToWordConverter = new PdfToWordConverter();

    @PostMapping("/upload/files")
    public ResponseEntity<ByteArrayResource> uploadFiles(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // 1) turn MultipartFile into a File
            File javaFile = toJavaFile(file);
            // 2) convert PDF → Word
            File wordFile = pdfToWordConverter.convertToWord(javaFile);
            // 3) read all bytes of the .docx
            byte[] data = Files.readAllBytes(wordFile.toPath());
            ByteArrayResource resource = new ByteArrayResource(data);

            // 4) stream it back as a download
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + wordFile.getName() + "\"")
                    .contentLength(data.length)
                    .body(resource);

        } catch (IOException e) {
            // you can return a more detailed error if you like
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

