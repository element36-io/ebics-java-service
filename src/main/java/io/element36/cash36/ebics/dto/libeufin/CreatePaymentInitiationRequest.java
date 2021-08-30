package io.element36.cash36.ebics.dto.libeufin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentInitiationRequest {
    
    String iban;
    String bic;
    String name;
    String amount;
    String subject;
    
}
