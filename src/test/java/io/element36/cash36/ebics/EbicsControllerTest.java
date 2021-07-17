package io.element36.cash36.ebics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import io.element36.cash36.ebics.controller.EbicsController;
import io.element36.cash36.ebics.dto.PaymentStatusReportDTO;
import io.element36.cash36.ebics.dto.StatementDTO;

/**
 *  gradle -x checkstyleMain test --tests io.element36.cash36.ebics.EbicsControllerTest.testGetStatusReport
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class EbicsControllerTest {

  @Autowired EbicsController controller;

  @Test
  public void testGetStatusReport() throws Exception {

    ResponseEntity<List<PaymentStatusReportDTO>> statusReport = controller.getStatusReport();
    assertThat(statusReport).isNotNull();
    assertThat(statusReport.getBody()).isNotNull();
  }

  @Test
  public void testGetPayments() throws Exception {

    ResponseEntity<List<StatementDTO>> payments = controller.getPayments();
    assertThat(payments).isNotNull();
    assertThat(payments.getBody()).isNotNull();

    assertThat(payments.getBody().size()).isGreaterThan(0);
    for (StatementDTO item : payments.getBody()) {
      System.out.println("-----------------------");
      TestTool.pp(item);
    }
    		// test content
		TestTool.testProxyStatements(payments.getBody());
  }
}
