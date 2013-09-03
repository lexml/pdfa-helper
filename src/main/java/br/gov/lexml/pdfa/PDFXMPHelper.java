package br.gov.lexml.pdfa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class PDFXMPHelper {

	public static void main(String[] args) throws IOException, DocumentException{
		setXmpMetadata("target/rendition.pdf",
				"target/renditionFINAL.pdf",
				FileUtils.readFileToByteArray(new File("src/test/resources/metadata.txt")));
	}
	
	public static void setXmpMetadata(String sourceFileName, String toFileName, byte[] xmp) throws IOException, DocumentException {
        
		PdfReader reader = new PdfReader(sourceFileName);
		
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(toFileName));
        
        createPDFWithXmpMetadata(stamper, xmp);
        
        stamper.close();
	}

	public static void createPDFWithXmpMetadata(OutputStream out, InputStream inputStream, byte[] xmp) throws IOException, DocumentException {
        
		PdfReader reader = new PdfReader(inputStream);
        
        PdfStamper stamper = new PdfStamper(reader, out);
        
        createPDFWithXmpMetadata(stamper, xmp);
        
        stamper.close();
	}
	
	public static PdfStamper createPDFWithXmpMetadata(PdfStamper stamper, byte[] xmp) throws IOException, DocumentException {
        
        stamper.setXmpMetadata(xmp);
        
        return stamper;
        
	}



}