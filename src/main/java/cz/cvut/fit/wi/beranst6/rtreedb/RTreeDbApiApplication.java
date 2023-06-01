package cz.cvut.fit.wi.beranst6.rtreedb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class RTreeDbApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RTreeDbApiApplication.class, args);
	}

}
