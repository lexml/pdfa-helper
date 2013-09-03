package br.gov.lexml.pdfa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfAWriter;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfFileSpecification;
import com.itextpdf.text.pdf.PdfName;

public class CreatePDFA3 {

	public static final String RESULT = "target/pdfa3.pdf";
	public static final String FONTPATH = "/usr/share/fonts/truetype/msttcorefonts/times.ttf";
    private static Font FONT = FontFactory.getFont(FONTPATH, BaseFont.CP1252, BaseFont.EMBEDDED);
	
	public static void main(String args[]) throws DocumentException, IOException {

        Document document = new Document();
        
     	PdfAWriter writer = PdfAWriter.getInstance(document, new FileOutputStream(RESULT), PdfAConformanceLevel.PDF_A_3B);
     	
        writer.createXmpMetadata();
        
        document.open();
        
        PdfFileSpecification fs = 
        		PDFAttachmentHelper.addAttachment(
        				writer, 
        				new PDFAttachmentFile(
        						new File("src/main/resources/madoc.jpeg"), 
        						"madoc.jpeg", 
        						"image/jpeg", 
        						"2012/01/01T01:01-02:00", 
        						PDFAttachmentFile.AFRelationShip.SOURCE));
        
        writer.getExtraCatalog().put(new PdfName("AF"), new PdfArray(fs.getReference()));

        document.add(new Paragraph("Hello World!", FONT));
        
        document.close();
        
        System.out.println("Pronto!");
	}
	
}
