import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by deepglint on 16/7/13.
 */
@SpringBootApplication
@ComponentScan("/")
public class Application {

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }
}
