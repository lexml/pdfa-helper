package br.gov.lexml.pdfa;

import java.io.File;

public class PDFAttachmentFile{
	
	public enum AFRelationShip {
		SOURCE("Source"),
		DATA("Data"),
		ALTERNATIVE("Alternative"),
		SUPPLEMENT("Supplement"),
		UNSPECIFIED("Unspecified");
		
		private final String value;
		
		private AFRelationShip(String value){
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}
	
	private File file;
	private byte fileStore[];
	private String fileName;
	private String mimeType;
	private String dateTime;
	private AFRelationShip afRelationship;
	
	public PDFAttachmentFile(File file, String fileName, String mimeType, String dateTime, AFRelationShip afRelationship){
		this(file, null, fileName, mimeType, dateTime, afRelationship);
	}
	
	public PDFAttachmentFile(byte fileStore[], String fileName, String mimeType, String dateTime, AFRelationShip afRelationship){
		this(null, fileStore, fileName, mimeType, dateTime, afRelationship);
	}
	
	public PDFAttachmentFile(File file, byte fileStore[], String fileName, String mimeType, String dateTime, AFRelationShip afRelationship){
		this.file = file;
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.dateTime = dateTime;
		this.afRelationship = afRelationship;
		this.fileStore = fileStore; 
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getDateTime() {
		return dateTime;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public String getAfRelationshipValue() {
		return afRelationship.getValue();
	}
	
	public byte[] getFileStore() {
		return fileStore;
	}
}