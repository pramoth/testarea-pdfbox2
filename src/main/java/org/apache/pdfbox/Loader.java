package org.apache.pdfbox;

import java.io.*;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.util.XMLUtil;

/**
 * Utility methods to load different types of documents
 */
public class Loader {
    private Loader() {
    }
    public static PDDocument loadPDF(InputStream inputStream) throws IOException {
        return PDDocument.load(inputStream);
    }
    public static PDDocument loadPDF(File inputFile) throws IOException {
        return PDDocument.load(new FileInputStream(inputFile));
    }
    public static PDDocument loadPDF(byte[] input) throws IOException {
        return PDDocument.load(new ByteArrayInputStream(input));
    }
    public static PDDocument loadPDF(InputStream input,MemoryUsageSetting memoryUsageSetting) throws IOException {
        return PDDocument.load(input,memoryUsageSetting);
    }
}