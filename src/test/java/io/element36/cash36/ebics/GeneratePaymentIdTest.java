package io.element36.cash36.ebics;

import static io.element36.cash36.ebics.TestTool.pp;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.element36.cash36.ebics.strategy.GeneratePaymentIds;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GeneratePaymentIdTest {

    @Autowired
	GeneratePaymentIds paymentIds;

    @Test
	public void testMsgId() throws Exception {
       String id= paymentIds.getMsgId(null, null);
       pp("tx-id"+id);
       assertThat(id).isNotNull();
       assertThat(id.length()).isBetween(10,250);
    }

    @Test
	public void testPmtInfId() throws Exception {
       String id= paymentIds.getPmtInfId(null, null);
       pp("tx-id"+id);
       assertThat(id).isNotNull();
       assertThat(id.length()).isBetween(1,250);
    }
 
}
