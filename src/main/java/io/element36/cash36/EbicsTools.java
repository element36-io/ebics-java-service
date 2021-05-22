package io.element36.cash36;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EbicsTools {

	
	
    public void printContent(File file) {
    	try {
			log.debug("ebics - content of file {} is:\n{}",file.getName(),Files.readAllBytes(file.toPath()).toString());
		} catch (IOException e) {
			log.error("ERROR Utils.printContent ",e);
		}
    }
    
    public String getContent(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()));
    }
    
	
	
	public List<File> unzip(File pathToZip, String destDirStr) throws IOException {
		File destDir = new File(destDirStr);
		destDir.mkdirs();
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(pathToZip));
		ZipEntry zipEntry = zis.getNextEntry();
		List<File> newFiles = new ArrayList<>();
		while (zipEntry != null) {
			File newFile = newFile(destDir, zipEntry);
			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			zipEntry = zis.getNextEntry();

			// Add file to collection
			newFiles.add(newFile);
		}
		zis.closeEntry();
		zis.close();
		return newFiles;
	}

	public File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	public boolean moveFile(String src, String dest) {
		Path result = null;
		try {
			result = Files.move(Paths.get(src), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("Exception while moving file: " + e.getMessage());
		}
		if (result != null) {
			return true;
		} else {
			return false;
		}
	}

	public XMLGregorianCalendar getXmlGregorianCalendarDate(String format) throws DatatypeConfigurationException {
        DateFormat df = new SimpleDateFormat(format);
        XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(df.format(new Date()));
        return xmlDate;
    }
}
