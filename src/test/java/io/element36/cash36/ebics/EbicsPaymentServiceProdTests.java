package io.element36.cash36.ebics;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import io.element36.cash36.ebics.service.EbicsPaymentService;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"ebics.mode=disabled","ebics.outputDir=./out"})
@ActiveProfiles("prod")
public class EbicsPaymentServiceProdTests {
	
    @Autowired
    EbicsPaymentService ebicsPaymentService;
	
	@Test
	public void makePayment() throws Exception {
		//	    String makePayment(String msgId, String pmtInfId, String sourceIban, String sourceBic, BigDecimal amount,
		//                String currency, String receipientIban, String receipientBankName, String recipientBankPostAccount,
		//                String receipientName, String purpose, String ourReference, String receipientStreet, String receipientStreetNr,
		//                String receipientZip, String receipientCity, String receipientCountry,
		//                String clearingSystemMemberId, boolean nationalPayment) throws Exception;
		// values taken from	
		// https://www.iban.com/structure
		
		String statusMessage=ebicsPaymentService.makePayment("4711", "abc", "BE71096123456769", "", new BigDecimal(100), 
				"EUR", "DE75512108001245126199", "Testbank", "", 
				"Test Person", "Test Purpose", "our Ref", "Rec Street", "Street-No.", 
				"1000", "TestCity", "DE", 
				"clearingSystemMemberId", false);
		
		System.out.println(statusMessage);
		String fileName=statusMessage.split(":")[1];
		fileName=fileName.split(";")[0];
		String content=TestTool.readLineByLineJava8(fileName);
		//System.out.println("Payment File content:\n:"+content);
		content=TestTool.findAndReplaceTagContent("CreDtTm", "2021-07-16T14:43:03", content);
		content=TestTool.findAndReplaceTagContent("ReqdExctnDt", "2021-07-16", content);		
		org.assertj.core.api.Assertions.assertThat(content).isEqualTo(TestTool.PAIN1);

		
	}
	
		
	@Test
	public void simulatePayment() throws Exception {
		//	    String makePayment(String msgId, String pmtInfId, String sourceIban, String sourceBic, BigDecimal amount,
		//                String currency, String receipientIban, String receipientBankName, String recipientBankPostAccount,
		//                String receipientName, String purpose, String ourReference, String receipientStreet, String receipientStreetNr,
		//                String receipientZip, String receipientCity, String receipientCountry,
		//                String clearingSystemMemberId, boolean nationalPayment) throws Exception;
		// values taken from	
		// https://www.iban.com/structure
		
		String statusMessage=ebicsPaymentService.simulatePayment("4711", "abc", "BE71096123456769", "", new BigDecimal(100), 
				"EUR", "DE75512108001245126199", "Testbank", "", 
				"Test Person", "Test Purpose", "our Ref", "Rec Street", "Street-No.", 
				"1000", "TestCity", "DE", 
				"clearingSystemMemberId", false);

		System.out.println(statusMessage);
		String fileName=statusMessage.split(":")[4];
		fileName=fileName.split(";")[0];
		
		String content=TestTool.readLineByLineJava8(fileName);
		System.out.println("Payment File content:\n:"+content);
		content=TestTool.findAndReplaceTagContent("CreDtTm", "2021-07-16T15:12:31", content);
		content=TestTool.findAndReplaceTagContent("ReqdExctnDt", "2021-07-16", content);
		org.assertj.core.api.Assertions.assertThat(content).isEqualTo(TestTool.PAIN2);
			}

}
