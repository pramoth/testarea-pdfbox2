package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class AddSpecialCharacterWithoutEmbedding {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/57295834/without-embeded-fonts-is-pdf-limited-to-only-4281-characters-of-agl-how-to-d">
     * Without embeded fonts, is PDF limited to only 4281 characters (of AGL)? How to display more glyphs?
     * </a>
     * <p>
     * This test constructs a type 1 font without embedded font program
     * that uses a non AGL name in its encoding's Differences array to
     * provide a glyph not to be found on the AGL. This is a counterexample
     * to the OP's original question.
     * </p>
     */
    @Test
    public void testWithArial() throws IOException {
        try (   PDDocument doc = new PDDocument();
                FileOutputStream os = new FileOutputStream(new File(RESULT_FOLDER, "SpecialCharacterWithArial.pdf"))    ) {
            COSDictionary fontDict = buildUnembeddedArialWithSpecialEncoding();
            PDTrueTypeFont font = new PDTrueTypeFont(fontDict);
            PDPage page = new PDPage();
            doc.addPage(page);
            try (   PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 24);
                contentStream.newLineAtOffset(30, 600);
                contentStream.appendRawCommands("( ) Tj\n");
                contentStream.endText();
            }

            doc.save(os);
        }
    }

    COSDictionary buildUnembeddedArialWithSpecialEncoding() {
        COSArray differences = new COSArray();
        differences.add(COSInteger.get(32));
        differences.add(COSName.getPDFName("uniAB55"));

        COSDictionary fontDescDict = new COSDictionary();
        fontDescDict.setName("Type", "FontDescriptor");
        fontDescDict.setName("FontName", "Arial");
        fontDescDict.setString("FontFamily", "Arial");
        fontDescDict.setInt("Flags", 32);
        fontDescDict.setItem("FontBBox", new PDRectangle(-665, -325, 2665, 1365));
        fontDescDict.setInt("ItalicAngle", 0);
        fontDescDict.setInt("Ascent", 1040);
        fontDescDict.setInt("Descent", -325);
        fontDescDict.setInt("CapHeight", 716);
        fontDescDict.setInt("StemV", 88);
        fontDescDict.setInt("XHeight", 519);

        COSDictionary encodingDict = new COSDictionary();
        encodingDict.setName("Type", "Encoding");
        encodingDict.setName("BaseEncoding", "WinAnsiEncoding");
        encodingDict.setItem("Differences", differences);

        COSArray widths = new COSArray();
        widths.add(COSInteger.get(500));

        COSDictionary fontDict = new COSDictionary();
        fontDict.setName("Type", "Font");
        fontDict.setName("Subtype", "TrueType");
        fontDict.setName("BaseFont", "Arial");
        fontDict.setInt("FirstChar", 32);
        fontDict.setInt("LastChar", 32);
        fontDict.setItem("Widths", widths);
        fontDict.setItem("FontDescriptor", fontDescDict);
        fontDict.setItem("Encoding", encodingDict);

        return fontDict;
    }
}
