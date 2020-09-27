package mkl.testarea.pdfbox2.extract;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * <a href="https://stackoverflow.com/questions/63936154/how-to-identify-and-remove-hidden-text-from-the-pdf-using-pdfbox-java">
 * How to identify and remove hidden text from the PDF using PDFBox java
 * </a>
 * <p>
 * This class extends the <code>PDFTextStripper</code> by an option to
 * filter individual <code>TextPosition</code> instances.
 * </p>
 * 
 * @author mkl
 */
public class PDFFilteringTextStripper extends PDFTextStripper {
    public interface TextStripperFilter {
        public boolean accept(TextPosition text, PDGraphicsState graphicsState);
    }

    public PDFFilteringTextStripper(TextStripperFilter filter) throws IOException {
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetStrokingColorSpace());
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorSpace());
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetStrokingColor());
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColor());
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetStrokingColorN());
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorN());
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceGrayColor());
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceGrayColor());
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceRGBColor());
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceRGBColor());
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceCMYKColor());
        addOperator(new org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceCMYKColor());

        this.filter = filter;
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        PDGraphicsState graphicsState = getGraphicsState();
        if (filter.accept(text, graphicsState))
            super.processTextPosition(text);
    }

    final TextStripperFilter filter;
}
