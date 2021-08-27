package io.element36.cash36.ebics.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.element36.cash36.ebics.strategy.Pain;
import io.element36.cash36.ebics.strategy.PaymentStatus;
import io.element36.cash36.ebics.strategy.Statement;
import io.element36.cash36.ebics.strategy.impl.PainCH;
import io.element36.cash36.ebics.strategy.impl.PaymentStatusCH;
import io.element36.cash36.ebics.strategy.impl.StatementCamt53_001;

@Configuration
@ConfigurationProperties(prefix = "ebics.libeufin")
public class AppConfig {
    
  public static final String API_PATH = "api-v1";

  @Value("${ebics.entrypoint}")
  public String entryPoint;

  @Value("${ebics.mode}")
  EbicsMode ebicsMode;

  @Value("${ebics.peggingAccount.iban}")
  public String peggingIban;

  @Value("${ebics.peggingAccount.bic}")
  public String peggingBic;

  List<String> ignoreAccounts;

  public List<String> getIgnoreAccounts() {
    return this.ignoreAccounts;
  }

  public void setIgnoreAccounts(List<String> p) {
    this.ignoreAccounts = p;
  }

  @Value("${ebics.outputDir}")
  public String outputDir;

  @Bean
  public EbicsMode ebicsMode() {
    return ebicsMode;
  }

  @Bean
  public PaymentStatus paymentStrategy() {
    return new PaymentStatusCH();
  }

  @Bean
  public Statement statementStrategy() {
    return new StatementCamt53_001();
  }

  @Bean
  public Pain painStrategy() {
    return new PainCH();
  }
}
