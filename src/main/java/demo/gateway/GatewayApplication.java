package demo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(GatewayApplication.UriConfiguration.class)
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
//	@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes().build();
	}
	@Bean
	public RouteLocator myRoutesEnable(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				.route(p -> p.path("/get")
						.filters(f -> f.addRequestHeader("Hello", "World"))
						.uri("http://httpbin.org:80"))
				.build();
	}
	
//	@Bean
	public RouteLocator circuitBreakerTimeout(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				.route(p -> p
						.path("/get")
						.filters(f -> f.addRequestHeader("Hello", "World"))
						.uri("http://httpbin.org:80"))
				.route(p -> p
						.host("*.circuitbreaker.com")
						.filters(f -> f.circuitBreaker(config -> config.setName("mycmd")))
						.uri("http://httpbin.org:80"))
				.build();
	}
	
//	@Bean
	public RouteLocator circuitBreakerFallback(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				.route(p -> p
						.path("/get")
						.filters(f -> f.addRequestHeader("Hello", "World"))
						.uri("http://httpbin.org:80"))
				.route(p -> p
						.host("*.circuitbreaker.com")
						.filters(f -> f.circuitBreaker(config -> config
								.setName("mycmd")
								.setFallbackUri("forward:/fallback")))
						.uri("http://httpbin.org:80"))
				.build();
	}
	
	// fallback endpoint 설정

	
	@ConfigurationProperties
	static class UriConfiguration{
		private String httpbin = "http://httpbin.org:80";
		
		public String getHttpbin() {
			return httpbin;
		}
		public void setHttpbin(String httpbin) {
			this.httpbin = httpbin;
		}
	}
	@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
		String httpUri = uriConfiguration.getHttpbin();
		return builder.routes()
				.route(p -> p
						.path("/get")
						.filters(f -> f.addRequestHeader("Hello", "world"))
						.uri(httpUri))
				.route(p -> p
						.host("*.circuitbreaker.com")
						.filters(f -> f.circuitBreaker(config -> config
								.setName("mycmd")
								.setFallbackUri("/fallback")))
						.uri(httpUri))
				.build();
	}
	
}
