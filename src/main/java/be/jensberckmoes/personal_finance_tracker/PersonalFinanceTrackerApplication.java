package be.jensberckmoes.personal_finance_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "be.jensberckmoes.personal_finance_tracker")
public class PersonalFinanceTrackerApplication {

	public static void main(final String[] args) {
		SpringApplication.run(PersonalFinanceTrackerApplication.class, args);
	}

}
