package br.gov.lexml.pdfa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfAStamper;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfFileSpecification;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFAttachmentHelper {

	private static final PdfName AF_KEY = new PdfName("AF");
	
	/*
	 * TEST
	 */
	
	public static void main(String[] args) throws IOException, DocumentException{
	
		PDFAttachmentFile f = new PDFAttachmentFile(
				new File("src/main/resources/madoc.jpeg"), 
				"madoc.jpeg", 
				"image/jpeg", 
				"2012/01/01T01:01-02:00", 
				PDFAttachmentFile.AFRelationShip.SOURCE);
		
		List<PDFAttachmentFile> listAttachments = new ArrayList<PDFAttachmentFile>();
		listAttachments.add(f);
		
		PDFAttachmentHelper.addAttachmentsToPDF(
				new FileInputStream("target/rendition.pdf"), 
				new FileOutputStream("target/rendition-a3.pdf"), 
				listAttachments,
				PdfAConformanceLevel.PDF_A_3B);

        System.out.println("Pronto!");
	}
	
	/*
	 * ADD ATTACHMENTS
	 */
	
	/**
	 * Open a PDF, add attachments files to and close it.
	 * Reference: http://support.itextpdf.com/node/130
	 * 
	 * @param pdfSource
	 * @param dest
	 * @param files
	 * @param conformanceLevel
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static void addAttachmentsToPDF(InputStream pdfSource, OutputStream dest,
			List<PDFAttachmentFile> files, PdfAConformanceLevel conformanceLevel) throws IOException, DocumentException {
		
		PdfReader reader = new PdfReader(pdfSource);
		
		PdfAStamper stamper = new PdfAStamper(reader, dest, conformanceLevel);
		
		List<PdfFileSpecification> pfs = addAttachmentsToPdfWriter(stamper.getWriter(), files);
		
		addAFEntry(stamper.getWriter(), pfs);
		
		stamper.close();
	}
	
	/**
	 * Add attachments files to a PdfWriter
	 * @param writer
	 * @param files
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static List<PdfFileSpecification> addAttachmentsToPdfWriter(PdfWriter writer, List<PDFAttachmentFile> files) throws IOException, DocumentException {
		
		List<PdfFileSpecification> pdfFileSpecificationList = new ArrayList<PdfFileSpecification>();
		
		for (PDFAttachmentFile f : files){
			pdfFileSpecificationList.add(addAttachment(writer, f));
		}
		
		return pdfFileSpecificationList;
	}

	
	/**
	 * PDF/A-3: Put an AF entry field in a PdfWriter. 
	 * @param writer
	 * @param pdfFileSpecificationList
	 * @throws IOException
	 */
	protected static void addAFEntry(PdfWriter writer, List<PdfFileSpecification> pdfFileSpecificationList) throws IOException{
		addAFEntry(writer.getExtraCatalog(), pdfFileSpecificationList);
	}
	
	/**
	 * PDF/A-3: Put an AF entry field in a PdfDictionary (used with PdfReader).
	 * @param catalog
	 * @param pdfFileSpecificationList
	 * @throws IOException
	 */
	protected static void addAFEntry(PdfDictionary catalog, List<PdfFileSpecification> pdfFileSpecificationList) throws IOException{
		
		//load PDFCatalog
		//PdfDictionary catalog = writer.getExtraCatalog();
		
		//load AF Key if it exists
		PdfArray pdfArray = catalog.getAsArray(AF_KEY);
		if (pdfArray == null){
			pdfArray = new PdfArray();
		}

		//add PdfFileSpecification to array
		for (PdfFileSpecification pfs: pdfFileSpecificationList){
			pdfArray.add(pfs.getReference());
		}

		//replace AF key on catalog
		catalog.put(AF_KEY, pdfArray);
	}

	


	
	/**
	 * Fonte: http://support.itextpdf.com/node/130
	 * 
	 * @param writer
	 * @param file
	 * @throws IOException
	 */
	protected static PdfFileSpecification addAttachment(PdfWriter writer, PDFAttachmentFile file)
			throws IOException {
		
		PdfDictionary fileParameter = new PdfDictionary();

		fileParameter.put(new PdfName("ModDate"), new PdfString(file.getDateTime()));

		PdfFileSpecification fs = PdfFileSpecification.fileEmbedded(
				writer,
				(file.getFile() == null ? null : file.getFile().getAbsolutePath()),
				file.getFileName(), 
				file.getFileStore(),
				false, 
				file.getMimeType(), 
				fileParameter);
		
		fs.put(new PdfName("AFRelationship"), new PdfName(file.getAfRelationshipValue()));
		
		writer.addFileAttachment(file.getFileName(), fs);
		
		return fs;
	}

	/*
	 * EXTRACT ATTACHMENTS
	 */
	
	
	/**
	 * Fonte: http://support.itextpdf.com/node/130
	 * @param src
	 * @param dir
	 * @throws IOException
	 */
	public static void extractAttachments(String src, String dir) throws IOException {
		File folder = new File(dir);
		folder.mkdirs();
		PdfReader reader = new PdfReader(src);
		PdfDictionary root = reader.getCatalog();
		PdfDictionary names = root.getAsDict(PdfName.NAMES);
		PdfDictionary embedded = names.getAsDict(PdfName.EMBEDDEDFILES);
		PdfArray filespecs = embedded.getAsArray(PdfName.NAMES);
		for (int i = 0; i < filespecs.size();) {
			extractAttachment(reader, folder, filespecs.getAsString(i++),
					filespecs.getAsDict(i++));
		}
	}

	/**
	 * Fonte: http://support.itextpdf.com/node/130
	 * @param reader
	 * @param dir
	 * @param name
	 * @param filespec
	 * @throws IOException
	 */
	protected static void extractAttachment(PdfReader reader, File dir,
			PdfString name, PdfDictionary filespec) throws IOException {
		PRStream stream;
		FileOutputStream fos;
		String filename;
		PdfDictionary refs = filespec.getAsDict(PdfName.EF);
		for (PdfName key : refs.getKeys()) {
			stream = (PRStream) PdfReader.getPdfObject(refs.getAsIndirectObject(key));
			filename = filespec.getAsString(key).toString();
			fos = new FileOutputStream(new File(dir, filename));
			fos.write(PdfReader.getStreamBytes(stream));
			fos.flush();
			fos.close();
		}
	}

	/**
	 * Fonte:
	 * http://itext-general.2136553.n4.nabble.com/Managing-PDF-attachments
	 * -with-iText-td2163977.html
	 * 
	 * @param reader
	 */
	public static void removeAttachments(PdfReader reader) {
		PdfDictionary catalog = reader.getCatalog();
		PdfDictionary names = (PdfDictionary) PdfReader.getPdfObject(catalog.get(PdfName.NAMES));
		if (names != null) {
			PdfDictionary files = (PdfDictionary) PdfReader.getPdfObject(names.get(PdfName.EMBEDDEDFILES));
			if (files != null) {
				for (Object key : files.getKeys()) {
					files.remove((PdfName) key);
				}
				reader.removeUnusedObjects();
			}
		}
	}
	
}
