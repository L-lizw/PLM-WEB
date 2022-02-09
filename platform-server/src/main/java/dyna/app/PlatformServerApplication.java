package dyna.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Lizw
 * @date 2022/1/23
 **/
@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {"dyna.common","dyna.app"})
public class PlatformServerApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(PlatformServerApplication.class, args);
	}
}
