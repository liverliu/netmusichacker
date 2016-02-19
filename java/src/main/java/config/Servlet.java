package config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by liverliu on 1/12/16.
 */
@Configuration
@EnableWebMvc
@ComponentScan("api")
public class Servlet extends WebMvcConfigurerAdapter {

}
