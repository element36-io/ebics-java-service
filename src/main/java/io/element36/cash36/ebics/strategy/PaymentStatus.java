package io.element36.cash36.ebics.strategy;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.element36.cash36.ebics.dto.PaymentStatusReportDTO;

public interface PaymentStatus {

    public  List<PaymentStatusReportDTO> process(File z01OutFile) throws IOException;
    
}
