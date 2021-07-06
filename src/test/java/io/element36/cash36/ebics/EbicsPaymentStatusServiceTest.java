package io.element36.cash36.ebics;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.element36.cash36.ebics.dto.PaymentStatusReportDTO;
import io.element36.cash36.ebics.service.EbicsPaymentStatusService;

@RunWith(SpringRunner.class)
@SpringBootTest

public class EbicsPaymentStatusServiceTest {

	@Autowired
	EbicsPaymentStatusService ebicsPaymentStatusService;
	
	@Test
	public void getStatus() {
		List<PaymentStatusReportDTO> transactions=ebicsPaymentStatusService.getStatusReport();
		for (PaymentStatusReportDTO tx:transactions) {
			System.out.println("msg-id: "+tx.getMsgId());
		}
	}
}
