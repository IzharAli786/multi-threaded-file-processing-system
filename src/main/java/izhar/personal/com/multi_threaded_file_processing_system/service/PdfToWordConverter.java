package izhar.personal.com.multi_threaded_file_processing_system.service;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.AutoCloseable;

public class PdfToWordConverter  {
    public File convertToWord(File pdfFile) throws IOException {
        String pdfPath = pdfFile.getAbsolutePath();
        File docxFile = new File(pdfPath.replaceAll("\\.pdf$", ".docx"));
        StringBuilder allText = new StringBuilder();
        PdfReader reader = new PdfReader(pdfPath);
        try  {
            int pages = reader.getNumberOfPages();
            for (int i = 1; i <= pages; i++) {
                String text = PdfTextExtractor.getTextFromPage(
                        reader, i, new SimpleTextExtractionStrategy()
                );
                allText.append(text).append(System.lineSeparator());
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read PDF", e);
        }
        finally {

            reader.close();
        }
        // 2) Write to a fresh .docx
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(docxFile)) {

            for (String line : allText.toString().split("\\r?\\n")) {
                if (line.isBlank()) continue;
                XWPFParagraph p = document.createParagraph();
                p.createRun().setText(line);
            }
            document.write(out);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write DOCX", e);
        }
        return docxFile;
    }

}