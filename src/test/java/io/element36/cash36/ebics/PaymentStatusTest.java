package io.element36.cash36.ebics;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.element36.cash36.ebics.dto.PaymentStatusReportDTO;
import io.element36.cash36.ebics.strategy.PaymentStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"ebics.mode=proxy"})
public class PaymentStatusTest {
	
	private final static String TMP_DIR=System.getProperty("java.io.tmpdir")+"/";
	
	@Autowired
	PaymentStatus paymentStatus;

	@Test
	public void testLoad() throws Exception {

		File tempFile = File.createTempFile(TMP_DIR+"test", ".xml"); 

  		// Writes a string to the above temporary file

  		Files.write(tempFile.toPath(), MSG.getBytes(StandardCharsets.UTF_8));		
		// zip it
		File zipFile=File.createTempFile(TMP_DIR+"test",".zip");

		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);

		zos.putNextEntry(new ZipEntry(tempFile.getName()));

		byte[] bytes = Files.readAllBytes(Paths.get(tempFile.getAbsolutePath()));
		zos.write(bytes, 0, bytes.length);
		zos.closeEntry();
		zos.close();

		List<PaymentStatusReportDTO> status = paymentStatus.process(zipFile);
		assertThat(status.size()).isEqualTo(1);
		


		
	}
	final static String MSG=  "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
								+ "  <Document xmlns=\"http://www.six-interbank-clearing.com/de/pain.002.001.03.ch.02.xsd\">\n"
								+ "  <CstmrPmtStsRpt>\n"
								+ "  <GrpHdr>\n"
								+ "<MsgId>XML990920150717170814238</MsgId>\n"
								+ "<CreDtTm>2015-07-17T15:08:14Z</CreDtTm>\n"
								+ "  <InitgPty>\n"
								+ "  <Id>\n"
								+ "  <OrgId>\n"
								+ "  <Othr>\n"
								+ "<Id>NDEATEST</Id>\n"
								+ "  <SchmeNm>\n"
								+ "<Cd>BANK</Cd>\n"
								+ "</SchmeNm>\n"
								+ "</Othr>\n"
								+ "  <Othr>\n"
								+ "<Id>1825226354</Id>\n"
								+ "  <SchmeNm>\n"
								+ "<Cd>CUST</Cd>\n"
								+ "</SchmeNm>\n"
								+ "</Othr>\n"
								+ "</OrgId>\n"
								+ "</Id>\n"
								+ "</InitgPty>\n"
								+ "</GrpHdr>\n"
								+ "  <OrgnlGrpInfAndSts>\n"
								+ "<OrgnlMsgId>MSGID-COM-SCE25-1707-01</OrgnlMsgId>\n"
								+ "<OrgnlMsgNmId>pain.001.001.03</OrgnlMsgNmId>\n"
								+ "<GrpSts>ACCP</GrpSts>\n"
								+ "</OrgnlGrpInfAndSts>\n"
								+ "  <OrgnlPmtInfAndSts>\n"
								+ "<OrgnlPmtInfId>PMTINFID-COM-SCE25-1707-01</OrgnlPmtInfId>\n"
								+ "<PmtInfSts>RJCT</PmtInfSts>\n"
								+ "  <StsRsnInf>\n"
								+ "  <Rsn>\n"
								+ "<Cd>NARR</Cd>\n"
								+ "</Rsn>\n"
								+ "<AddtlInf>CAP Invalid code or combinations in CategoryPurpose or ServiceLevel</AddtlInf>\n"
								+ "</StsRsnInf>\n"
								+ "</OrgnlPmtInfAndSts>\n"
								+ "</CstmrPmtStsRpt>"
								+ "</Document>";
   

}
