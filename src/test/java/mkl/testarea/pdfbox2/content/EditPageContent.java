package mkl.testarea.pdfbox2.content;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="https://stackoverflow.com/questions/58475104/filter-out-all-text-above-a-certain-font-size-from-pdf">
 * Filter out all text above a certain font size from PDF
 * </a>
 * <p>
 * This test class tests the {@link PdfContentStreamEditor}.
 * </p>
 * <p>
 * {@link PdfContentStreamEditor} is the base editor class which by default acts as the
 * identity operation (or at least it produces an equivalent content stream).
 * </p>
 * 
 * @author mkl
 */
public class EditPageContent {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * Test identity editing.
     */
    @Test
    public void testIdentityInput() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("input.pdf");
                PDDocument document = PDDocument.load(resource)) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfContentStreamEditor identity = new PdfContentStreamEditor(document, page);
                identity.processPage(page);
            }
            document.save(new File(RESULT_FOLDER, "input-identity.pdf"));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38498431/how-to-remove-filtered-content-from-a-pdf-with-itext">
     * How to remove filtered content from a PDF with iText
     * </a>
     * <br/>
     * <a href="https://1drv.ms/b/s!AmNST-TRoPSemi2k0UnGFsjQM1Yt">
     * document.pdf
     * </a>
     * <p>
     * This test shows how to remove text filtered by actual font size.
     * </p>
     */
    @Test
    public void testRemoveBigTextDocument() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("document.pdf");
                PDDocument document = PDDocument.load(resource)) {
            for (PDPage page : document.getDocumentCatalog().getPages()) {
                PdfContentStreamEditor identity = new PdfContentStreamEditor(document, page) {
                    @Override
                    protected void write(ContentStreamWriter contentStreamWriter, Operator operator, List<COSBase> operands) throws IOException {
                        String operatorString = operator.getName();

                        if (TEXT_SHOWING_OPERATORS.contains(operatorString))
                        {
                            float fs = getGraphicsState().getTextState().getFontSize();
                            Matrix matrix = getTextMatrix().multiply(getGraphicsState().getCurrentTransformationMatrix());
                            Point2D.Float transformedFsVector = matrix.transformPoint(0, fs);
                            Point2D.Float transformedOrigin = matrix.transformPoint(0, 0);
                            double transformedFs = transformedFsVector.distance(transformedOrigin);
                            if (transformedFs > 100)
                                return;
                        }

                        super.write(contentStreamWriter, operator, operands);
                    }

                    final List<String> TEXT_SHOWING_OPERATORS = Arrays.asList("Tj", "'", "\"", "TJ");
                };
                identity.processPage(page);
            }
            document.save(new File(RESULT_FOLDER, "document-noBigText.pdf"));
        }
    }
}
