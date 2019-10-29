package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class DrawImage {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/58606529/pdf-size-too-large-generating-through-android-pdfdocument-and-while-using-pdfbo">
     * PDF size too large generating through Android PDFDocument. And while using pdfbox it is cutting image in output
     * </a>
     * <p>
     * This code shows how to draw an image onto a page with
     * the image "default size".
     * </p>
     */
    @Test
    public void testDrawImageToFitPage() throws IOException {
        try (   InputStream imageResource = getClass().getResourceAsStream("Willi-1.jpg")) {
            PDDocument document = new PDDocument();

            PDImageXObject ximage = JPEGFactory.createFromStream(document,imageResource);

            PDPage page = new PDPage(new PDRectangle(ximage.getWidth(), ximage.getHeight()));
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.drawImage(ximage, 0, 0);
            contentStream.close();

            document.save(new File(RESULT_FOLDER, "Willi-1.pdf"));
            document.close();
        }
    }

}
