package trillion9.studyarcade_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class StudyArcadeBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudyArcadeBeApplication.class, args);
	}

}
