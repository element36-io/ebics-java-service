package io.element36.cash36.ebics.strategy.impl;

import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import io.element36.cash36.ebics.dto.UnpegPayment;
import io.element36.cash36.ebics.strategy.GeneratePaymentIds;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GeneratePaymentIdsImpl implements GeneratePaymentIds {
	
	
	@Override
    public String getMsgId(UnpegPayment r, HttpServletRequest req) {
		return this.getid(r, req);
	}
	
	@Override
    public String getPmtInfId(UnpegPayment r, HttpServletRequest req) {
		return this.getid(r, req);
	}
	
    private String getid(UnpegPayment r, HttpServletRequest req) {
    	int paymentNo=this.getNextPaymentNo();
    	String id= "UP-"+paymentNo+"-"+new Date().getTime()+"-"+new Random().nextInt(99999);
    	log.info("generated payment id "+id);
    	return id;
    }
    

	
    private Integer currentPaymentNo=0;
    
    public int getNextPaymentNo() {
    	synchronized (currentPaymentNo) {
    		currentPaymentNo++;	
    		return currentPaymentNo;
		}
    }

}
