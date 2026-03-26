package be.gate25.tokencontext;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the transaction token starter.
 *
 * <p>Activated automatically when the JAR is on the classpath and the application
 * is a Servlet-based web application. No {@code @Import} or {@code @ComponentScan}
 * needed in the consuming project.
 *
 * <h3>Registered beans</h3>
 * <ul>
 *   <li>{@link TokenFilter} — always registered (can be overridden with your own bean)</li>
 *   <li>{@link GlobalExceptionHandler} — registered by default, disable with
 *       {@code token-context.exception-handler.enabled=false}</li>
 * </ul>
 *
 * <h3>Customisation</h3>
 * <p>To replace the filter with your own implementation, simply declare a
 * {@code @Bean} of type {@link TokenFilter} in your application — the
 * {@code @ConditionalOnMissingBean} will back off automatically.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class TokenContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TokenFilter tokenFilter() {
        return new TokenFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            name    = "token-context.exception-handler.enabled",
            havingValue = "true",
            matchIfMissing = true   // enabled by default
    )
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}