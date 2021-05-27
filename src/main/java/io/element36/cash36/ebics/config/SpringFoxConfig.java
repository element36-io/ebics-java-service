package io.element36.cash36.ebics.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SpringFoxConfig {         
	
	@Value("${server.servlet.context-path}")
	String path;
	
    @Bean
    public Docket api() { 
    	String selector=path+"/"+AppConfig.API_PATH+"/.*";
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          //.apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
          .apis(RequestHandlerSelectors.basePackage("io.element36.cash36.ebics.controller"))
          //.paths(PathSelectors.any())
          .paths(PathSelectors.regex(selector))    
          .build()
          .apiInfo(apiInfo());
    }

	private ApiInfo apiInfo() {
	  return new ApiInfo(
	    "Rest Wrapper for ebics-java-client", 
	    "Access your bank account based on EBICS inface over a simple REST-API.", 
	    "API terms of service - see Apache 2.0 licence", 
	    "https://www.apache.org/licenses/LICENSE-2.0", 
	    new Contact("element36", "element36.io", "ask@element36.io"), 
	    "Apache 2.0", "https://www.apache.org/licenses/LICENSE-2.0", Collections.emptyList());
	}
}