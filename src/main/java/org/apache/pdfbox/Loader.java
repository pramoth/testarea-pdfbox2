package org.apache.pdfbox;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.*;

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