package mkl.testarea.pdfbox2.content;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CurvedBorderRectangle {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/60942642/cant-get-path-to-fill-in-pdfbox">
     * Can't Get Path to Fill in PDFBox
     * </a>
     * <p>
     * The reason for this code not creating a filled rectangle is
     * that the path contains many <code>moveTo</code> instructions
     * effectively splitting the path into a multitude of subpaths
     * each of which is filled by itself but none is encompassing
     * the whole rectangle.
     * </p>
     * @see #testLikeMaht33nImproved()
     */
    @Test
    public void testLikeMaht33n() throws IOException {
        try (   PDDocument document = new PDDocument()  ) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (   PDPageContentStream contentStream = new PDPageContentStream(document, page)    ) {
                float x = 100;
                float y = 100;
                float width = 200;
                float height = 300;

                contentStream.setStrokingColor( Color.BLACK );
                contentStream.setNonStrokingColor( Color.BLACK );

                // bottom of rectangle
                contentStream.moveTo(x - 0.5f, y );
                contentStream.lineTo(x + width + 0.5f, y );
                contentStream.moveTo(x + width, y );

                contentStream.curveTo(x + width + 5.9f, y + 0.14f,
                                      x + width + 11.06f, y + 5.16f,
                                      x + width + 10.96f, y + 10);

                // left of rectangle
                contentStream.moveTo(x, y );
                contentStream.curveTo(x - 5.9f, y + 0.14f,
                                      x - 11.06f, y + 5.16f,
                                      x - 10.96f, y + 10);
                contentStream.moveTo(x - 10.96f, y + 10 - 0.5f);
                contentStream.lineTo(x - 10.96f, y + height + 0.5f );


                // right of rectangle       
                contentStream.moveTo(x + width + 10.96f, y + 10 - 0.5f);
                contentStream.lineTo(x + width + 10.96f, y + height + 0.5f);
                contentStream.moveTo(x + width, y + height + 10);
                contentStream.curveTo(x + width + 5.9f, y + height + 0.14f + 10,
                          x + width + 11.06f, y + height - 5.16f + 10,
                          x + width + 10.96f, y + height);

                // top of rectangle
                contentStream.moveTo(x + width + 0.5f, y + height + 10);
                contentStream.lineTo(x - 0.5f, y + height + 10);
                contentStream.moveTo(x, y + height + 10);
                contentStream.curveTo(x - 5.9f, y + height + 0.14f + 10,
                          x - 11.06f, y + height - 5.16f + 10,
                          x - 10.96f, y + height);

                contentStream.closePath();
                contentStream.fill();
            }
            document.save(new File(RESULT_FOLDER, "CurvedBorderRectangleLikeMaht33n.pdf"));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/60942642/cant-get-path-to-fill-in-pdfbox">
     * Can't Get Path to Fill in PDFBox
     * </a>
     * <p>
     * This improves the OP's code from {@link #testLikeMaht33n()}
     * by drawing the whole rectangle outline in one stroke with
     * no <code>moveTo</code> but the initial one. This required
     * changing directions of some lines and curves. Now filling
     * the path results in the desired filled rectangle.
     * </p>
     */
    @Test
    public void testLikeMaht33nImproved() throws IOException {
        try (   PDDocument document = new PDDocument()  ) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (   PDPageContentStream contentStream = new PDPageContentStream(document, page)    ) {
                float x = 100;
                float y = 100;
                float width = 200;
                float height = 300;

                contentStream.setStrokingColor( Color.BLACK );
                contentStream.setNonStrokingColor( Color.BLACK );

                contentStream.moveTo(x, y);

                // bottom of rectangle, left to right
                contentStream.lineTo(x + width, y );
                contentStream.curveTo(x + width + 5.9f, y + 0.14f,
                        x + width + 11.06f, y + 5.16f,
                        x + width + 10.96f, y + 10);

                // right of rectangle, bottom to top
                contentStream.lineTo(x + width + 10.96f, y + height);
                contentStream.curveTo(x + width + 11.06f, y + height - 5.16f + 10,
                        x + width + 5.9f, y + height + 0.14f + 10,
                        x + width, y + height + 10);

                // top of rectangle, right to left
                contentStream.lineTo(x, y + height + 10);
                contentStream.curveTo(x - 5.9f, y + height + 0.14f + 10,
                        x - 11.06f, y + height - 5.16f + 10,
                        x - 10.96f, y + height);

                // left of rectangle, top to bottom
                contentStream.lineTo(x - 10.96f, y + 10);
                contentStream.curveTo(x - 11.06f, y + 5.16f,
                        x - 5.9f, y + 0.14f,
                        x, y);

                contentStream.closePath();
                contentStream.fill();
            }
            document.save(new File(RESULT_FOLDER, "CurvedBorderRectangleLikeMaht33n-improved.pdf"));
        }
    }
}
