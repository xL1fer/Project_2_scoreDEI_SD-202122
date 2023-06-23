package scoreDEI.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@ComponentScan({"scoreDEI.*"})
@EnableJpaRepositories("scoreDEI.*")
@EntityScan("scoreDEI.*")
public class AppApplication extends SpringBootServletInitializer{

	// handle Invalid URL exceptions
	@Configuration
	public class WebApplicationConfig implements WebMvcConfigurer {
	
		@Override
		public void addViewControllers(ViewControllerRegistry registry) {
			registry.addViewController("/notFound").setViewName("forward:/index.html");
		}
		
		@Bean
		public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> containerCustomizer() {
			return container -> {
				container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND,
						"/pageNotFound"));
			};
		}
	
	}

	// handle "Bad Request 400" exceptions
	@ControllerAdvice
	public class ExceptionHandler {
	 
		@org.springframework.web.bind.annotation.ExceptionHandler
		@ResponseStatus(HttpStatus.BAD_REQUEST)
		@ResponseBody
		public ModelAndView handleCustomRuntimeException(HttpServletRequest request, HttpServletResponse response, Exception ex) {
			return new ModelAndView("/pageNotFound");
		}
	}

	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(AppApplication.class);
    }

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

}
