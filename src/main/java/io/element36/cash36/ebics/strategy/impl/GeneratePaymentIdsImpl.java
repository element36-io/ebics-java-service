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
    	String ip=req.getRemoteAddr();
    	int paymentNo=this.getNextPaymentNo();
    	//InetAddress i= InetAddress.getByName(ip);
    	//int intRepresentation= ByteBuffer.wrap(i.getAddress()).getInt();
    	String id= "UP-"+paymentNo+"-"+ip+"-"+new Date().getTime()+"-"+new Random().nextInt(10000);
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
