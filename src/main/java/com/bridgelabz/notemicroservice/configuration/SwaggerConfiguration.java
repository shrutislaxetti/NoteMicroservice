package com.bridgelabz.notemicroservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

	@Bean
	public Docket postsApi() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("public-api")
				.apiInfo(apiInfo()).select().paths(PathSelectors.any()).build();
	}

	@SuppressWarnings("deprecation")
	  private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("FundooNoteAppln").description("Taking notes using Spring Boot And MongoDB ")
                .contact("simranbodra6@gmail.com").version("1.0").build();
    }
	
	@Bean
	public SecurityConfiguration security() {
		return new SecurityConfiguration(null, null, null, null, "Authorization",
				ApiKeyVehicle.HEADER, "Authorization", ",");
	}

}