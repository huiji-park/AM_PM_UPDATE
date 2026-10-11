package phoenix.AM_PM;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@ConfigurationPropertiesScan
@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
public class AmPmApplication {

	public static void main(String[] args) {

		SpringApplication.run(AmPmApplication.class, args);
	}
}

