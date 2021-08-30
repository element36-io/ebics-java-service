package io.element36.cash36.ebics.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties(prefix = "ebics")
@Profile("sandbox")
public class AppConfigLibeufin {
    
  @Value("${ebics.libeufin.sandbox_url}")
  public String sandbox_url;

  @Value("${ebics.libeufin.nexus_url}")
  public String nexus_url;

  @Value("${ebics.libeufin.nexus_username}")
  public String username;

  @Value("${ebics.libeufin.nexus_password}")
  public String password;

  @Value("${ebics.libeufin.nexus_account_balance}")
  public BigDecimal accountBalance;

  @Value("${ebics.libeufin.nexus_account_currency}")
  public String accountCurrency;
}
