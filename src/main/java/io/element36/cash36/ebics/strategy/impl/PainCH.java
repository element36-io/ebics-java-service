package io.element36.cash36.ebics.strategy.impl;

import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;

import io.element36.cash36.EbicsTools;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.AccountIdentification4ChoiceCH;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.ActiveOrHistoricCurrencyAndAmount;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.AmountType3Choice;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.BranchAndFinancialInstitutionIdentification4CH;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.BranchAndFinancialInstitutionIdentification4CHBicOrClrId;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.CashAccount16CHId;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.CashAccount16CHIdTpCcy;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.ClearingSystemIdentification2Choice;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.ClearingSystemMemberIdentification2;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.CreditTransferTransactionInformation10CH;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.CustomerCreditTransferInitiationV03CH;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.Document;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.FinancialInstitutionIdentification7CH;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.FinancialInstitutionIdentification7CHBicOrClrId;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.GroupHeader32CH;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.LocalInstrument2Choice;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.ObjectFactory;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.PartyIdentification32CH;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.PartyIdentification32CHName;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.PartyIdentification32CHNameAndId;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.PaymentIdentification1;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.PaymentInstructionInformation3CH;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.PaymentMethod3Code;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.PaymentTypeInformation19CH;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.PostalAddress6CH;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.RemittanceInformation5CH;
import io.element36.cash36.ebics.generated.pain_001_001_03_ch_02.ServiceLevel8Choice;
import io.element36.cash36.ebics.strategy.Pain;

public class PainCH implements Pain {

  public JAXBElement<Document> generatePainFile(
      String msgId,
      String pmtInfId,
      String sourceIban,
      String sourceBic,
      BigDecimal amount,
      String currency,
      String receipientIban,
      String receipientBankName,
      String recipientBankPostAccount,
      String receipientName,
      String purpose,
      String ourReference,
      String receipientStreet,
      String receipientStreetNr,
      String receipientZip,
      String receipientCity,
      String receipientCountry,
      String clearingSystemMemberId,
      boolean nationalPayment)
      throws DatatypeConfigurationException, IOException {

    // Generate XML for Payment
    ObjectFactory factory = new ObjectFactory();
    Document document = new Document();
    CustomerCreditTransferInitiationV03CH customerCreditTransferInitiationV03CH =
        new CustomerCreditTransferInitiationV03CH();

    // Group Header - [1..1]
    GroupHeader32CH groupHeader32CH = new GroupHeader32CH();
    groupHeader32CH.setMsgId(msgId);
    groupHeader32CH.setCreDtTm(
        new EbicsTools().getXmlGregorianCalendarDate("yyyy-MM-dd'T'HH:mm:ss"));
    groupHeader32CH.setNbOfTxs("1");
    groupHeader32CH.setCtrlSum(amount);

    PartyIdentification32CHNameAndId partyIdentification32CHNameAndId =
        new PartyIdentification32CHNameAndId();
    partyIdentification32CHNameAndId.setNm("element36 AG");
    groupHeader32CH.setInitgPty(partyIdentification32CHNameAndId);

    // B-LEVEL - PaymentInstructionInformation - [1...unbound] - Main Object
    PaymentInstructionInformation3CH paymentInstructionInformation3CH =
        new PaymentInstructionInformation3CH();
    paymentInstructionInformation3CH.setPmtInfId(pmtInfId);
    paymentInstructionInformation3CH.setPmtMtd(PaymentMethod3Code.TRF);
    paymentInstructionInformation3CH.setBtchBookg(true);

    // Acc. Specification, recommended for EUR Payments international
    if (!nationalPayment) {
      PaymentTypeInformation19CH paymentTypeInformation19CH = new PaymentTypeInformation19CH();
      ServiceLevel8Choice serviceLevel8Choice = new ServiceLevel8Choice();
      serviceLevel8Choice.setCd("SEPA");
      paymentTypeInformation19CH.setSvcLvl(serviceLevel8Choice);
      // Update main object
      paymentInstructionInformation3CH.setPmtTpInf(paymentTypeInformation19CH);
    }

    // Execution Date
    paymentInstructionInformation3CH.setReqdExctnDt(
        new EbicsTools().getXmlGregorianCalendarDate("yyyy-MM-dd"));

    // Debitor - "Zahlungspflichtige" - element36 AG
    PartyIdentification32CH partyIdentification32CH = new PartyIdentification32CH();
    partyIdentification32CH.setNm("element36 AG");
    // Update main object
    paymentInstructionInformation3CH.setDbtr(partyIdentification32CH);

    // Debitor - Our IBAN Address - CHR or EUR Account - given via param from
    // Exchange
    CashAccount16CHIdTpCcy cashAccount16CHIdTpCcy = new CashAccount16CHIdTpCcy();
    AccountIdentification4ChoiceCH accountIdentification4ChoiceCH =
        new AccountIdentification4ChoiceCH();
    accountIdentification4ChoiceCH.setIBAN(sourceIban);
    cashAccount16CHIdTpCcy.setId(accountIdentification4ChoiceCH);
    // Update main object
    paymentInstructionInformation3CH.setDbtrAcct(cashAccount16CHIdTpCcy);

    // Our BIC for need account - passed as param
    BranchAndFinancialInstitutionIdentification4CHBicOrClrId
        branchAndFinancialInstitutionIdentification4CHBicOrClrId =
            new BranchAndFinancialInstitutionIdentification4CHBicOrClrId();
    FinancialInstitutionIdentification7CHBicOrClrId
        financialInstitutionIdentification7CHBicOrClrId =
            new FinancialInstitutionIdentification7CHBicOrClrId();
    financialInstitutionIdentification7CHBicOrClrId.setBIC(sourceBic);
    branchAndFinancialInstitutionIdentification4CHBicOrClrId.setFinInstnId(
        financialInstitutionIdentification7CHBicOrClrId);
    // Update main object
    paymentInstructionInformation3CH.setDbtrAgt(
        branchAndFinancialInstitutionIdentification4CHBicOrClrId);

    // C-LEVEL - Payment Information Creditor
    CreditTransferTransactionInformation10CH creditTransferTransactionInformation10CH =
        new CreditTransferTransactionInformation10CH();

    PaymentIdentification1 paymentIdentification1 = new PaymentIdentification1();
    paymentIdentification1.setEndToEndId("E2E-" + msgId); // Our Reference
    paymentIdentification1.setInstrId("INSTRID-" + msgId);
    creditTransferTransactionInformation10CH.setPmtId(paymentIdentification1);

    // Payment Type - moved to C-Level
    if (nationalPayment) {
      PaymentTypeInformation19CH paymentTypeInformation19CH = new PaymentTypeInformation19CH();
      LocalInstrument2Choice localInstrument2Choice = new LocalInstrument2Choice();
      localInstrument2Choice.setPrtry(
          "CH03"); // Payment Type using IBAN, National, Art 2.2 aak Spec
      paymentTypeInformation19CH.setLclInstrm(localInstrument2Choice);

      // Update main object
      creditTransferTransactionInformation10CH.setPmtTpInf(paymentTypeInformation19CH);
    }

    // Amount to be payed - National: CHF or EUR, International: EUR
    AmountType3Choice amountType3Choice = new AmountType3Choice();
    ActiveOrHistoricCurrencyAndAmount activeOrHistoricCurrencyAndAmount =
        new ActiveOrHistoricCurrencyAndAmount();
    activeOrHistoricCurrencyAndAmount.setCcy(currency);
    activeOrHistoricCurrencyAndAmount.setValue(amount);
    amountType3Choice.setInstdAmt(activeOrHistoricCurrencyAndAmount);
    // Update main object - C-LEVEL
    creditTransferTransactionInformation10CH.setAmt(amountType3Choice);

    // Receipient Information - passed from Exchange
    PartyIdentification32CHName partyIdentification32CHName = new PartyIdentification32CHName();
    partyIdentification32CHName.setNm(receipientName);

    PostalAddress6CH postalAddress6CH1 = new PostalAddress6CH();
    if (nationalPayment) {
      postalAddress6CH1.setStrtNm(receipientStreet);
      postalAddress6CH1.setBldgNb(receipientStreetNr);
      postalAddress6CH1.setPstCd(receipientZip);
      postalAddress6CH1.setTwnNm(receipientCity);
      postalAddress6CH1.setCtry(receipientCountry);
    }
    if (!nationalPayment) {
      String addrLine1 = String.format("%s %s", receipientStreet, receipientStreetNr);
      String addrLine2 = String.format("%s %s", receipientZip, receipientCity);
      postalAddress6CH1.setCtry(receipientCountry);
      postalAddress6CH1.getAdrLine().add(addrLine1);
      postalAddress6CH1.getAdrLine().add(addrLine2);
    }
    partyIdentification32CHName.setPstlAdr(postalAddress6CH1);
    // Update main object - C-LEVEL
    creditTransferTransactionInformation10CH.setCdtr(partyIdentification32CHName);

    // Creditor Account - "Zahlungsempfänger" - Recipient
    CashAccount16CHId cashAccount16CHId = new CashAccount16CHId();
    AccountIdentification4ChoiceCH accountIdentification4ChoiceCH1 =
        new AccountIdentification4ChoiceCH();
    accountIdentification4ChoiceCH1.setIBAN(receipientIban);
    cashAccount16CHId.setId(accountIdentification4ChoiceCH1);
    // Update main object - C-LEVEL
    creditTransferTransactionInformation10CH.setCdtrAcct(cashAccount16CHId);

    // Creditor Agent
    if (nationalPayment) {
      // Update main object - C-LEVEL
      BranchAndFinancialInstitutionIdentification4CH
          branchAndFinancialInstitutionIdentification4CH =
              new BranchAndFinancialInstitutionIdentification4CH();
      FinancialInstitutionIdentification7CH financialInstitutionIdentification7CH =
          new FinancialInstitutionIdentification7CH();

      ClearingSystemMemberIdentification2 clrSysMmbId = new ClearingSystemMemberIdentification2();
      ClearingSystemIdentification2Choice clrSysId = new ClearingSystemIdentification2Choice();
      clrSysId.setCd("CHBCC");
      clrSysMmbId.setClrSysId(clrSysId);
      clrSysMmbId.setMmbId(clearingSystemMemberId);
      financialInstitutionIdentification7CH.setClrSysMmbId(clrSysMmbId);

      branchAndFinancialInstitutionIdentification4CH.setFinInstnId(
          financialInstitutionIdentification7CH);
      creditTransferTransactionInformation10CH.setCdtrAgt(
          branchAndFinancialInstitutionIdentification4CH);
    }

    // Remittance Information - For Art 2.2 and Art 5 we use unstructured format -
    // Max 140 chars
    RemittanceInformation5CH remittanceInformation5CH = new RemittanceInformation5CH();
    remittanceInformation5CH.setUstrd(purpose);
    // Update main object - C-LEVEL
    creditTransferTransactionInformation10CH.setRmtInf(remittanceInformation5CH);

    // Add all to final document
    customerCreditTransferInitiationV03CH.setGrpHdr(groupHeader32CH);
    paymentInstructionInformation3CH.getCdtTrfTxInf().add(creditTransferTransactionInformation10CH);
    customerCreditTransferInitiationV03CH.getPmtInf().add(paymentInstructionInformation3CH);
    document.setCstmrCdtTrfInitn(customerCreditTransferInitiationV03CH);

    JAXBElement<Document> jaxbElement = factory.createDocument(document);

    return jaxbElement;
  }

  public String getSchemaLocation() {
    return "http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd  pain.001.001.03.ch.02.xsd";
  }
}
