package izhar.personal.com.multi_threaded_file_processing_system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.InputStream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FileUploadIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithMockUser(username = "izhar", roles = {"USER"})
  void testSingleFileUpload() throws Exception {
    // Load test PDF from resources
    InputStream pdfStream = getClass().getClassLoader().getResourceAsStream("IzharCv.pdf");

    if (pdfStream == null) {
      throw new RuntimeException("Test PDF file not found in resources");
    }

    MockMultipartFile file = new MockMultipartFile(
          "file",
          "test.pdf",
          "application/pdf",
          pdfStream.readAllBytes()
    );

    MvcResult result = mockMvc.perform(multipart("/customer/upload/files")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isOk())
          .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("attachment")))
          .andReturn();
    // Verify response content type for single file
    String contentType = result.getResponse().getContentType();
    System.out.println("Response Content-Type: " + contentType);

    // Should be Word document for single file
    assert contentType != null;
    assert contentType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    System.out.println("✅ Single file upload test passed!");
  }

  @Test
  @WithMockUser(username = "izhar", roles = {"USER"})
  void testMultipleFileUpload() throws Exception {
    // Load test PDF from resources
    InputStream pdfStream1 = getClass().getClassLoader().getResourceAsStream("IzharCv.pdf");
    InputStream pdfStream2 = getClass().getClassLoader().getResourceAsStream("IzharCv.pdf");

    if (pdfStream1 == null || pdfStream2 == null) {
      throw new RuntimeException("Test PDF files not found in resources");
    }

    MockMultipartFile file1 = new MockMultipartFile(
          "file",
          "test1.pdf",
          "application/pdf",
          pdfStream1.readAllBytes()
    );

    MockMultipartFile file2 = new MockMultipartFile(
          "file",
          "test2.pdf",
          "application/pdf",
          pdfStream2.readAllBytes()
    );

    MvcResult result = mockMvc.perform(multipart("/customer/upload/files")
                .file(file1)
                .file(file2)

                .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isOk())
          .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("attachment")))
          .andReturn();

    // Verify response content type for multiple files (should be ZIP)
    String contentType = result.getResponse().getContentType();
    System.out.println("Response Content-Type: " + contentType);

    // Should be ZIP or octet-stream for multiple files
    assert contentType != null;
    assert contentType.contains("application/octet-stream") || contentType.contains("application/zip");

    System.out.println("✅ Multiple file upload test passed!");
  }

  @Test
  void testUploadWithoutAuth() throws Exception {
    InputStream pdfStream = getClass().getClassLoader().getResourceAsStream("IzharCv.pdf");

    if (pdfStream == null) {
      throw new RuntimeException("Test PDF file not found in resources");
    }

    MockMultipartFile file = new MockMultipartFile(
          "file",
          "test.pdf",
          "application/pdf",
          pdfStream.readAllBytes()
    );

    // Should return 401 without authentication
    mockMvc.perform(multipart("/customer/upload/files")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isUnauthorized());

    System.out.println("✅ Authentication test passed!");
  }

  @Test
  @WithMockUser(username = "izhar", roles = {"USER"})
  void testUploadEmptyFile() throws Exception {
    MockMultipartFile emptyFile = new MockMultipartFile(
          "file",
          "empty.pdf",
          "application/pdf",
          new byte[0]
    );

    mockMvc.perform(multipart("/customer/upload/files")
                .file(emptyFile)

                .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isBadRequest());

    System.out.println("✅ Empty file test passed!");
  }
}
