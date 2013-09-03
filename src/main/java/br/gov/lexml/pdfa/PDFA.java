package br.gov.lexml.pdfa;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfAStamper;
import com.itextpdf.text.pdf.PdfFileSpecification;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFA {

	private final PdfAStamper stamper;
	private final PdfReader reader;
	private final PdfAConformanceLevel conformanceLevel;
	
	public enum PDFVersion {
		
		PDF_VERSION_1_2(PdfWriter.PDF_VERSION_1_2),
		PDF_VERSION_1_3(PdfWriter.PDF_VERSION_1_3),
		PDF_VERSION_1_4(PdfWriter.PDF_VERSION_1_4),
		PDF_VERSION_1_5(PdfWriter.PDF_VERSION_1_5),
		PDF_VERSION_1_6(PdfWriter.PDF_VERSION_1_6),
		PDF_VERSION_1_7(PdfWriter.PDF_VERSION_1_7);
		
		private PdfName pdfNameVersion;
		
		private PDFVersion(PdfName pdfNameVersion){
			this.pdfNameVersion = pdfNameVersion;
		}
		
		public PdfName getPdfNameVersion(){
			return pdfNameVersion;
		}
	}
	
	public enum PDFConformance {
		
		PDF_A_1A (PdfAConformanceLevel.PDF_A_1A, "1", "A"),
		PDF_A_1B (PdfAConformanceLevel.PDF_A_1B, "1", "B"),
		PDF_A_2A (PdfAConformanceLevel.PDF_A_2A, "2", "A"),
		PDF_A_2B (PdfAConformanceLevel.PDF_A_2B, "2", "B"),
		PDF_A_3A (PdfAConformanceLevel.PDF_A_3A, "3", "A"),
		PDF_A_3B (PdfAConformanceLevel.PDF_A_3B, "3", "B");
				
		private final PdfAConformanceLevel conformanceLevel;
		private final String part;
		private final String conformance;
		
		private PDFConformance(PdfAConformanceLevel conformanceLevel, String part, String conformance){
			this.conformanceLevel = conformanceLevel;
			this.part = part;
			this.conformance = conformance;
		}
		
		public PdfAConformanceLevel getConformanceLevel() {
			return conformanceLevel;
		}
		
		public String getConformance() {
			return conformance;
		}
		
		public String getPart() {
			return part;
		}
		
		public static PDFConformance getPDFConformance(String part, String conformance){
			
			for (PDFConformance c : Arrays.asList(PDFConformance.values())){
				if (c.getPart().equals(part) && c.getConformance().equals(conformance)){
					return c;
				}
			}
			
			return null;
		}
	}
		
	private PDFA(PdfReader reader, PdfAStamper stamper, PdfAConformanceLevel conformanceLevel){
		this.reader = reader;
		this.stamper = stamper;
		this.conformanceLevel = conformanceLevel;
	}
	
	/**
	 * Load a PDF to be transformed as PDF/A3-B
	 * @param pdfSource
	 * @param pdfDest
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static PDFA getNewInstance(OutputStream pdfDest, InputStream pdfSource, String part, String conformance) throws Exception{
		return getNewInstance(pdfDest, pdfSource, PDFConformance.getPDFConformance(part, conformance));
	}
	
	/**
	 * Load a PDF to be transformed as a PdfAConformanceLevel
	 * @param pdfSource
	 * @param pdfDest
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	private static PDFA getNewInstance(OutputStream pdfDest, InputStream pdfSource, PDFConformance conformance) throws Exception{
		PdfAConformanceLevel c = conformance.getConformanceLevel();
		
		if (c == null){
			return null;
		}
		
		PdfReader reader = new PdfReader(pdfSource);
		PdfAStamper stamper = new PdfAStamper(reader, pdfDest, c);
		return new PDFA(reader, stamper, c);
	} 
	
	/**
	 * Return current PdfAStamper
	 * @return
	 */
	public PdfAStamper getStamper() {
		return stamper;
	}
	
	public void setVersion(PDFVersion pdfVersion){
		PdfWriter writer = stamper.getWriter();
		writer.setPdfVersion(pdfVersion.getPdfNameVersion());
		
	}
	
	/**
	 * Add a XMP metadata
	 * @param xmp
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public PDFA addXMP(byte[] xmp) throws Exception{
		
		PDFXMPHelper.createPDFWithXmpMetadata(getStamper(), xmp);
		
		return this;
	}
	
	/**
	 * Add an attachment
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public PDFA addAttachment(PDFAttachmentFile file) throws Exception{
		return addAttachments(file);
	}
	
	/**
	 * Add a list of attachments
	 * @param files
	 * @return
	 * @throws Exception
	 */
	public PDFA addAttachments(PDFAttachmentFile... files) throws Exception{
		return addAttachments(Arrays.asList(files));
	}
	
	/**
	 * Add a list of attachments
	 * @param files
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public PDFA addAttachments(List<PDFAttachmentFile> files) throws Exception{
	
		PdfWriter writer = getStamper().getWriter();
		
		List<PdfFileSpecification> pfs = PDFAttachmentHelper.addAttachmentsToPdfWriter(writer, files);
		
		if (conformanceLevel == PdfAConformanceLevel.PDF_A_3A || conformanceLevel == PdfAConformanceLevel.PDF_A_3B){
			PDFAttachmentHelper.addAFEntry(reader.getCatalog(), pfs);
		}
		
		return this;
	}
	
	/**
	 * Close PDF Stamper
	 * @param pdfAStamper
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void close() throws Exception{
		getStamper().close();
	}
	
}
