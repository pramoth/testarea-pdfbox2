package mkl.testarea.pdfbox2.content;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDFormContentStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroupAttributes;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 * @author mkl
 */
public class UseSoftMask {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/60198179/creating-a-transparency-group-or-setting-graphics-state-soft-mask-with-pdfbox">
     * Creating a transparency group or setting graphics state soft mask with PDFBox
     * </a>
     * <br/>
     * <a href="https://i.stack.imgur.com/rh9kL.png">
     * rh9kL.png
     * </a> as "Nicolas_image.png"
     * <br/>
     * <a href="https://i.stack.imgur.com/2UoKr.png">
     * 2UoKr.png
     * </a> as "Nicolas_mask.png"
     * <p>
     * This test demonstrates how one can apply transparency group
     * soft masks in extended graphics state parameters.
     * </p>
     */
    @Test
    public void testSoftMaskedImageAndRectangle() throws IOException {
        try (   PDDocument document = new PDDocument()  ) {
            final PDImageXObject image;
            try (   InputStream imageResource = getClass().getResourceAsStream("Nicolas_image.png")) {
                image = PDImageXObject.createFromByteArray(document, IOUtils.toByteArray(imageResource), "image");
            }

            final PDImageXObject mask;
            try (   InputStream imageResource = getClass().getResourceAsStream("Nicolas_mask.png")) {
                mask = PDImageXObject.createFromByteArray(document, IOUtils.toByteArray(imageResource), "mask");
            }

            PDTransparencyGroupAttributes transparencyGroupAttributes = new PDTransparencyGroupAttributes();
            transparencyGroupAttributes.getCOSObject().setItem(COSName.CS, COSName.DEVICEGRAY);

            PDTransparencyGroup transparencyGroup = new PDTransparencyGroup(document);
            transparencyGroup.setBBox(PDRectangle.A4);
            transparencyGroup.setResources(new PDResources());
            transparencyGroup.getCOSObject().setItem(COSName.GROUP, transparencyGroupAttributes);
            try (   PDFormContentStream canvas = new PDFormContentStream(transparencyGroup)   ) {
                canvas.drawImage(mask, new Matrix(400, 0, 0, 400, 100, 100));
            }

            COSDictionary softMaskDictionary = new COSDictionary();
            softMaskDictionary.setItem(COSName.S, COSName.LUMINOSITY);
            softMaskDictionary.setItem(COSName.G, transparencyGroup);

            PDExtendedGraphicsState extendedGraphicsState = new PDExtendedGraphicsState();
            extendedGraphicsState.getCOSObject().setItem(COSName.SMASK, softMaskDictionary);

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (   PDPageContentStream canvas = new PDPageContentStream(document, page)   ) {
                canvas.saveGraphicsState();
                canvas.setGraphicsStateParameters(extendedGraphicsState);
                canvas.setNonStrokingColor(Color.BLACK);
                canvas.addRect(100, 100, 400, 400);
                canvas.fill();
                canvas.drawImage(image, new Matrix(400, 0, 0, 300, 100, 150));
                canvas.restoreGraphicsState();
            }

            document.save(new File(RESULT_FOLDER, "SoftMaskedImageAndRectangle.pdf"));
        }
    }

}
