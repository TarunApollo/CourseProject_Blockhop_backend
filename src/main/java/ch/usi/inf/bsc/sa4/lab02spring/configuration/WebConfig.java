package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the CheckUserInterceptor for /users/view endpoint.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final CheckUserInterceptor checkUserInterceptor;

    @Autowired
    public WebConfig(CheckUserInterceptor checkUserInterceptor) {
        this.checkUserInterceptor = checkUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // checks the interceptor, if true it will redirect to users/me.
        //These are all the routes that need auth to be accessed
        registry.addInterceptor(checkUserInterceptor)
                .addPathPatterns("/users/search");
        registry.addInterceptor(checkUserInterceptor)
                .addPathPatterns("/users/{id}");
        registry.addInterceptor(checkUserInterceptor)
                .addPathPatterns("/users/changePassword");

    }
    
}
