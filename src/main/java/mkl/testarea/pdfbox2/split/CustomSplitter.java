package mkl.testarea.pdfbox2.split;

import java.util.Arrays;

import org.apache.pdfbox.multipdf.Splitter;

/**
 * <a href="https://stackoverflow.com/questions/58345483/how-to-separate-pdf-based-on-given-intervals">
 * How to separate pdf based on given intervals
 * </a>
 * <p>
 * This custom {@link Splitter} splits at the given page numbers.
 * </p>
 * @author mkl
 */
public class CustomSplitter extends Splitter {
    public CustomSplitter(int[] splitIndices) {
        this.splitIndices = splitIndices;
    }

    @Override
    protected boolean splitAtPage(int pageNumber) {
        return Arrays.binarySearch(splitIndices, pageNumber) >= 0;
    }

    final int[] splitIndices;
}
