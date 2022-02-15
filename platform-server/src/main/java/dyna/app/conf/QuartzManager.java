package dyna.app.conf;

import dyna.app.conf.yml.ConfigurableServerImpl;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author Lizw
 * @date 2022/2/1
 **/
@Configuration
public class QuartzManager
{

	@Autowired
	private           ConfigurableServerImpl svConfig;


	@Bean("timerScheduler")
	public Scheduler createScheduler() throws IOException
	{
		Scheduler scheduler = this.schedulerFactoryBean().getScheduler();
		return scheduler;
	}

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() throws IOException
	{
		SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();


		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation( new ClassPathResource("quartz-spring.properties"));
		propertiesFactoryBean.afterPropertiesSet();
		Properties quartzProperties = propertiesFactoryBean.getObject();

		factoryBean.setQuartzProperties(quartzProperties);

		factoryBean.setTaskExecutor(this.createExecutor());
		try
		{
			factoryBean.afterPropertiesSet();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return factoryBean;
	}

	@Bean
	public Executor createExecutor()
	{
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
		threadPoolTaskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
		threadPoolTaskExecutor.setQueueCapacity(Runtime.getRuntime().availableProcessors());
		return threadPoolTaskExecutor;
	}

}
