package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class TrimContent {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/63486940/crop-page-with-pdfbox">
     * Crop page with PDFBox
     * </a>
     * <p>
     * This test demonstrates how to trim contents without using the
     * crop box.
     * </p>
     */
    @Test
    public void testTrimCengage1() throws IOException {
        PDRectangle box = new PDRectangle(200, 300, 300, 400);
        try (InputStream resource = getClass().getResourceAsStream("Cengage1.pdf")) {
            PDDocument document = Loader.loadPDF(resource);
            for (PDPage page : document.getPages()) {
                PDRectangle cropBox = page.getCropBox();
                try (PDPageContentStream canvas = new PDPageContentStream(document, page, AppendMode.APPEND, false, true)) {
                    canvas.setNonStrokingColor(1);
                    canvas.addRect(cropBox.getLowerLeftX(), cropBox.getLowerLeftY(), cropBox.getWidth(), cropBox.getHeight());
                    canvas.addRect(box.getLowerLeftX(), box.getLowerLeftY(), box.getWidth(), box.getHeight());
                    canvas.fillEvenOdd();
                }
            }
            document.save(new File(RESULT_FOLDER, "Cengage1-Trimmed.pdf"));
        }
    }

}
