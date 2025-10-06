package izhar.personal.com.multi_threaded_file_processing_system.service;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.context.annotation.Bean;

import java.io.*;
import java.lang.AutoCloseable;

public class PdfToWordConverter {
  public File convertToWord(File pdfFile) {
    String pdfPath = pdfFile.getAbsolutePath();
    File docxFile = new File(pdfPath.replaceFirst("(?i)\\.pdf$", ".docx"));

    PdfReader reader = null; // not AutoCloseable in iText 5
    try (XWPFDocument document = new XWPFDocument();
         OutputStream out = new BufferedOutputStream(new FileOutputStream(docxFile))) {

      // Use the filename constructor (random access, better for big PDFs than InputStream)
      reader = new PdfReader(pdfPath);

      int pages = reader.getNumberOfPages();
      for (int i = 1; i <= pages; i++) {
        String pageText = PdfTextExtractor.getTextFromPage(reader, i, new SimpleTextExtractionStrategy());

        // ONE paragraph per page + line breaks inside the same run (far fewer POI objects)
        XWPFParagraph para = document.createParagraph();
        XWPFRun run = para.createRun();

        String[] lines = pageText.split("\\r?\\n", -1);
        for (int li = 0; li < lines.length; li++) {
          if (li > 0) run.addBreak();
          if (!lines[li].isEmpty()) run.setText(lines[li]);
        }
        run.addBreak(); // spacer between pages
      }

      document.write(out);

    } catch (IOException e) {
      throw new UncheckedIOException("PDF->DOCX failed", e);
    } finally {
      if (reader != null) {
        reader.close(); // important: releases the underlying random-access resource
      }
    }

    return docxFile;
  }


}