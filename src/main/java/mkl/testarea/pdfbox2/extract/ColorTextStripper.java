package mkl.testarea.pdfbox2.extract;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * <a href="https://stackoverflow.com/questions/59031734/get-text-color-in-pdfbox">
 * Get text color in PDFBox
 * </a>
 * <p>
 * This actually is a port of the color text stripper class from my answer to
 * <a href="https://stackoverflow.com/questions/21430341/identifying-the-text-based-on-the-output-in-pdf-using-pdfbox">
 * Identifying the text based on the output in PDF using PDFBOX
 * </a>
 * to PDFBox 2.
 * </p>
 * 
 * @author mkl
 */
public class ColorTextStripper extends PDFTextStripper
{
    public ColorTextStripper() throws IOException
    {
        super();
        setSuppressDuplicateOverlappingText(false);

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
    }

    @Override
    protected void processTextPosition(TextPosition text)
    {
        renderingMode.put(text, getGraphicsState().getTextState().getRenderingMode());
        strokingColor.put(text, getGraphicsState().getStrokingColor().getComponents());
        nonStrokingColor.put(text, getGraphicsState().getNonStrokingColor().getComponents());

        super.processTextPosition(text);
    }

    Map<TextPosition, RenderingMode> renderingMode = new HashMap<TextPosition, RenderingMode>();
    Map<TextPosition, float[]> strokingColor = new HashMap<TextPosition, float[]>();
    Map<TextPosition, float[]> nonStrokingColor = new HashMap<TextPosition, float[]>();

    final static List<RenderingMode> FILLING_MODES = Arrays.asList(RenderingMode.FILL, RenderingMode.FILL_STROKE, RenderingMode.FILL_CLIP, RenderingMode.FILL_STROKE_CLIP);
    final static List<RenderingMode> STROKING_MODES = Arrays.asList(RenderingMode.STROKE, RenderingMode.FILL_STROKE, RenderingMode.STROKE_CLIP, RenderingMode.FILL_STROKE_CLIP);
    final static List<RenderingMode> CLIPPING_MODES = Arrays.asList(RenderingMode.FILL_CLIP, RenderingMode.STROKE_CLIP, RenderingMode.FILL_STROKE_CLIP, RenderingMode.NEITHER_CLIP);

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException
    {
        for (TextPosition textPosition: textPositions)
        {
            RenderingMode charRenderingMode = renderingMode.get(textPosition);
            float[] charStrokingColor = strokingColor.get(textPosition);
            float[] charNonStrokingColor = nonStrokingColor.get(textPosition);

            StringBuilder textBuilder = new StringBuilder();
            textBuilder.append(textPosition.getUnicode())
                       .append("{");

            if (FILLING_MODES.contains(charRenderingMode))
            {
                textBuilder.append("FILL:")
                           .append(toString(charNonStrokingColor))
                           .append(';');
            }
            
            if (STROKING_MODES.contains(charRenderingMode))
            {
                textBuilder.append("STROKE:")
                           .append(toString(charStrokingColor))
                           .append(';');
            }

            if (CLIPPING_MODES.contains(charRenderingMode))
            {
                textBuilder.append("CLIP;");
            }

            textBuilder.append("}");
            writeString(textBuilder.toString());
        }
    }

    String toString(float[] values)
    {
        if (values == null)
            return "null";
        StringBuilder builder = new StringBuilder();
        switch(values.length)
        {
        case 1:
            builder.append("GRAY"); break;
        case 3:
            builder.append("RGB"); break;
        case 4:
            builder.append("CMYK"); break;
        default:
            builder.append("UNKNOWN");
        }
        for (float f: values)
        {
            builder.append(' ')
                   .append(f);
        }

        return builder.toString();
    }
    
}
