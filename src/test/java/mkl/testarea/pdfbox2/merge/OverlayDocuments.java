package mkl.testarea.pdfbox2.merge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author mkl
 */
public class OverlayDocuments {
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://issues.apache.org/jira/browse/PDFBOX-4797">
     * Overlayed PDF file do not shows the difference
     * </a>
     * <br/>
     * <a href="https://issues.apache.org/jira/secure/attachment/12996277/10.pdf">
     * 10.pdf
     * </a>
     * <br/>
     * <a href="https://issues.apache.org/jira/secure/attachment/12996276/114.pdf">
     * 114.pdf
     * </a>
     * <p>
     * This test demonstrates how to use the blend mode when overlaying documents
     * for comparison.
     * </p>
     */
    @Test
    public void testOverlayWithMultiply() throws IOException {
        try (   InputStream file1 = getClass().getResourceAsStream("10.pdf");
                InputStream file2 = getClass().getResourceAsStream("114.pdf");
                PDDocument document1 = Loader.loadPDF(file1);
                PDDocument document2 = Loader.loadPDF(file2);
                Overlay overlayer = new Overlay()) {
            overlayer.setInputPDF(document1);
            overlayer.setAllPagesOverlayPDF(document2);
            try (   PDDocument result = overlayer.overlay(Collections.emptyMap()) ) {
                result.save(new File(RESULT_FOLDER, "10and114.pdf"));

                try (   PDPageContentStream canvas = new PDPageContentStream(result, result.getPage(5), AppendMode.PREPEND, false, false)) {
                    PDExtendedGraphicsState extGState = new PDExtendedGraphicsState();
                    extGState.setBlendMode(BlendMode.MULTIPLY);
                    canvas.setGraphicsStateParameters(extGState);
                }
                result.save(new File(RESULT_FOLDER, "10and114multiply.pdf"));
            }
        }
    }

}
