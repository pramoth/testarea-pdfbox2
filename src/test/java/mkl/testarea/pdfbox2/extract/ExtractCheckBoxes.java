package mkl.testarea.pdfbox2.extract;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Test;

import mkl.testarea.pdfbox2.extract.PdfCheckBoxFinder.CheckBox;

public class ExtractCheckBoxes {
    /**
     * <a href="https://stackoverflow.com/questions/64093610/extract-checkbox-value-out-of-pdf-1-7-using-pdfbox">
     * Extract Checkbox value out of PDF 1.7 using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/12O9W3SE4l4EZg7WArYoL3e6M579I7w3U/view?usp=sharing">
     * Updated_Form.pdf
     * </a>
     * <p>
     * This test illustrates how to use the {@link PdfCheckBoxFinder}
     * to find check boxes and their check state.
     * </p>
     */
    @Test
    public void testExtractFromUpdatedForm() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Updated_Form.pdf");
                PDDocument document = Loader.loadPDF(resource)  ) {
            for (PDPage page : document.getPages())
            {
                PdfCheckBoxFinder finder = new PdfCheckBoxFinder(page);
                finder.processPage(page);
                for (CheckBox checkBox : finder.getBoxes()) {
                    Point2D ll = checkBox.getLowerLeft();
                    Point2D ur = checkBox.getUpperRight();
                    String checked = checkBox.isChecked() ? "checked" : "not checked";
                    System.out.printf(Locale.ROOT, "* (%4.3f, %4.3f) - (%4.3f, %4.3f) - %s\n", ll.getX(), ll.getY(), ur.getX(), ur.getY(), checked);
                }
            }
        }
    }
}
