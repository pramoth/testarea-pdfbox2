package mkl.testarea.pdfbox2.sign;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateMultipleVisualizations implements SignatureInterface {
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    public static final String KEYSTORE = "keystores/demo-rsa2048.p12";
    public static final char[] PASSWORD = "demo-rsa2048".toCharArray(); 

    public static KeyStore ks = null;
    public static PrivateKey pk = null;
    public static Certificate[] chain = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        BouncyCastleProvider bcp = new BouncyCastleProvider();
        Security.insertProviderAt(bcp, 1);

        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String) ks.aliases().nextElement();
        pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        chain = ks.getCertificateChain(alias);
    }

    /**
     * <a href="https://stackoverflow.com/questions/52829507/multiple-esign-using-pdfbox-2-0-12-java">
     * Multiple esign using pdfbox 2.0.12 java?
     * </a>
     * <p>
     * This test demonstrates how to create a single signature in multiple signature
     * fields with one widget annotation each only referenced from a single page each
     * only. (Actually there is an extra invisible signature; it is possible to get
     * rid of it with some more code.)
     * </p>
     */
    @Test
    public void testCreateSignatureWithMultipleVisualizations() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/analyze/test-rivu.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testSignedMultipleVisualizations.pdf"));
                PDDocument pdDocument = Loader.loadPDF(resource)   )
        {
            PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();
            if (acroForm == null) {
                pdDocument.getDocumentCatalog().setAcroForm(acroForm = new PDAcroForm(pdDocument));
            }
            acroForm.setSignaturesExist(true);
            acroForm.setAppendOnly(true);
            acroForm.getCOSObject().setDirect(true);

            PDRectangle rectangle = new PDRectangle(100, 600, 300, 100);
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("Example User");
            signature.setLocation("Los Angeles, CA");
            signature.setReason("Testing");
            signature.setSignDate(Calendar.getInstance());
            pdDocument.addSignature(signature, this);

            for (PDPage pdPage : pdDocument.getPages()) {
                addSignatureField(pdDocument, pdPage, rectangle, signature);
            }

            pdDocument.saveIncremental(result);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/52829507/multiple-esign-using-pdfbox-2-0-12-java">
     * Multiple esign using pdfbox 2.0.12 java?
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1DKSApmjrT424wT92yBdynKUkvR00x9i2">
     * C10000000713071804294534.pdf
     * </a>
     * <p>
     * This test demonstrates how to create a single signature in multiple signature
     * fields with one widget annotation each only referenced from a single page each
     * only. (Actually there is an extra invisible signature; it is possible to get
     * rid of it with some more code.) It uses the test file provided by the OP.
     * </p>
     */
    @Test
    public void testCreateSignatureWithMultipleVisualizationsC10000000713071804294534() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("C10000000713071804294534.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "C10000000713071804294534-SignedMultipleVisualizations.pdf"));
                PDDocument pdDocument = Loader.loadPDF(resource)   )
        {
            PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();
            if (acroForm == null) {
                pdDocument.getDocumentCatalog().setAcroForm(acroForm = new PDAcroForm(pdDocument));
            }
            acroForm.setSignaturesExist(true);
            acroForm.setAppendOnly(true);
            acroForm.getCOSObject().setDirect(true);

            PDRectangle rectangle = new PDRectangle(100, 600, 300, 100);
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("Example User");
            signature.setLocation("Los Angeles, CA");
            signature.setReason("Testing");
            signature.setSignDate(Calendar.getInstance());
            pdDocument.addSignature(signature, this);

            for (PDPage pdPage : pdDocument.getPages()) {
                addSignatureField(pdDocument, pdPage, rectangle, signature);
            }

            pdDocument.saveIncremental(result);
        }
    }

    /**
     * Based on <code>org.apache.pdfbox.examples.signature.CreateVisibleSignature2.createVisualSignatureTemplate(PDDocument, int, PDRectangle)</code>
     * from the pdfbox examples artifact but severely simplified and now used as a method to create the actual signature fields, not merely a template.
     */
    void addSignatureField(PDDocument pdDocument, PDPage pdPage, PDRectangle rectangle, PDSignature signature) throws IOException {
        PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();
        List<PDField> acroFormFields = acroForm.getFields();

        PDSignatureField signatureField = new PDSignatureField(acroForm);
        signatureField.setValue(signature);
        PDAnnotationWidget widget = signatureField.getWidgets().get(0);
        acroFormFields.add(signatureField);

        widget.setRectangle(rectangle);
        widget.setPage(pdPage);

        // from PDVisualSigBuilder.createHolderForm()
        PDStream stream = new PDStream(pdDocument);
        PDFormXObject form = new PDFormXObject(stream);
        PDResources res = new PDResources();
        form.setResources(res);
        form.setFormType(1);
        PDRectangle bbox = new PDRectangle(rectangle.getWidth(), rectangle.getHeight());
        float height = bbox.getHeight();

        form.setBBox(bbox);
        PDFont font = PDType1Font.HELVETICA_BOLD;

        // from PDVisualSigBuilder.createAppearanceDictionary()
        PDAppearanceDictionary appearance = new PDAppearanceDictionary();
        appearance.getCOSObject().setDirect(true);
        PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
        appearance.setNormalAppearance(appearanceStream);
        widget.setAppearance(appearance);

        try (PDPageContentStream cs = new PDPageContentStream(pdDocument, appearanceStream))
        {
            // show background (just for debugging, to see the rect size + position)
            cs.setNonStrokingColor(Color.yellow);
            cs.addRect(-5000, -5000, 10000, 10000);
            cs.fill();

            float fontSize = 10;
            float leading = fontSize * 1.5f;
            cs.beginText();
            cs.setFont(font, fontSize);
            cs.setNonStrokingColor(Color.black);
            cs.newLineAtOffset(fontSize, height - leading);
            cs.setLeading(leading);
            cs.showText("Signature text ");
            cs.newLine();
            cs.showText("some additional Information");
            cs.newLine();
            cs.showText("let's keep talking");
            cs.endText();
        }

        pdPage.getAnnotations().add(widget);
        
        COSDictionary pageTreeObject = pdPage.getCOSObject(); 
        while (pageTreeObject != null) {
            pageTreeObject.setNeedToBeUpdated(true);
            pageTreeObject = (COSDictionary) pageTreeObject.getDictionaryObject(COSName.PARENT);
        }
    }

    /**
     * Copy of <code>org.apache.pdfbox.examples.signature.CreateSignatureBase.sign(InputStream)</code>
     * from the pdfbox examples artifact.
     */
    @Override
    public byte[] sign(InputStream content) throws IOException {
        try
        {
            List<Certificate> certList = new ArrayList<>();
            certList.addAll(Arrays.asList(chain));
            Store<?> certs = new JcaCertStore(certList);
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            org.bouncycastle.asn1.x509.Certificate cert = org.bouncycastle.asn1.x509.Certificate.getInstance(chain[0].getEncoded());
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(pk);
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, new X509CertificateHolder(cert)));
            gen.addCertificates(certs);
            CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
            CMSSignedData signedData = gen.generate(msg, false);
            return signedData.getEncoded();
        }
        catch (GeneralSecurityException | CMSException | OperatorCreationException e)
        {
            throw new IOException(e);
        }
    }

    /**
     * Copy of <code>org.apache.pdfbox.examples.signature.CMSProcessableInputStream</code>
     * from the pdfbox examples artifact.
     */
    static class CMSProcessableInputStream implements CMSTypedData
    {
        private InputStream in;
        private final ASN1ObjectIdentifier contentType;

        CMSProcessableInputStream(InputStream is)
        {
            this(new ASN1ObjectIdentifier(CMSObjectIdentifiers.data.getId()), is);
        }

        CMSProcessableInputStream(ASN1ObjectIdentifier type, InputStream is)
        {
            contentType = type;
            in = is;
        }

        @Override
        public Object getContent()
        {
            return in;
        }

        @Override
        public void write(OutputStream out) throws IOException, CMSException
        {
            // read the content only one time
            IOUtils.copy(in, out);
            in.close();
        }

        @Override
        public ASN1ObjectIdentifier getContentType()
        {
            return contentType;
        }
    }
}
