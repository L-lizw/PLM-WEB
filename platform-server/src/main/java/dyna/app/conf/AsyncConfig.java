package dyna.app.conf;

import dyna.app.core.sch.AsyncScheduler;
import dyna.app.conf.yml.ConfigurableServerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 多线程任务配置类
 * @author Lizw
 * @date 2022/1/26
 **/
@EnableAsync
@Configuration
public class AsyncConfig
{
	public static final int	DEFAULT_THREAD_SIZE				= 16;
	public static final String				MULTI_THREAD_QUEUED_TASK	= "MULTITHREADQUEUEDTASK";

	public static final String              SELECT_CONNECT              = "SelectConnect";

	public static final String              POS_SAVE                    = "POSSAVE";

	public static final String              MAIL_SEND                   = "MAIL_SEND";

	private final Map<String, AsyncScheduler> schedulers = new HashMap<>();


	@Autowired
	private ConfigurableServerImpl serverConfig = null;

	/***
	 * 多线程任务池
	 * @return
	 */
	@Bean(MULTI_THREAD_QUEUED_TASK)
	public Executor multiThreadQueueTask()
	{
		int threadPoolCount = ((this.serverConfig.getThreadPoolCount() == null || this.serverConfig.getThreadPoolCount().intValue() < 2) ? 16
			: this.serverConfig.getThreadPoolCount().intValue());

		int threadSize = serverConfig.getThreadPoolCount();
		if (threadSize < 1)
		{
			threadSize = DEFAULT_THREAD_SIZE;
		}

		return this.createScheduler(threadSize, MULTI_THREAD_QUEUED_TASK);
	}

	@Bean(SELECT_CONNECT)
	public Executor selectConnect()
	{
		return this.createScheduler(8, SELECT_CONNECT);
	}

	@Bean(POS_SAVE)
	public Executor posSave()
	{
		return this.createScheduler(16, POS_SAVE);
	}

	@Bean(MAIL_SEND)
	public Executor mailSend()
	{
		return this.createScheduler(4, MAIL_SEND);
	}

	protected Executor createScheduler(int threadSize, String threadNamePrefix)
	{
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		//核心线程数5：线程池创建时候初始化的线程数
		executor.setCorePoolSize(threadSize);
		//最大线程数5：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
		executor.setMaxPoolSize(threadSize);
		//缓冲队列500：用来缓冲执行任务的队列
		executor.setQueueCapacity(500);
		//允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
		executor.setKeepAliveSeconds(0);
		//线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
		executor.setThreadNamePrefix(threadNamePrefix);

		executor.initialize();
		return executor;
	}


	public void addScheduler(String schName, AsyncScheduler scheduler)
	{
		synchronized (schedulers)
		{
			if (!this.schedulers.containsKey(schName))
			{
				this.schedulers.put(schName, scheduler);
			}
		}
	}

	public AsyncScheduler getScheduler(String schName)
	{
		AsyncScheduler scheduler = this.schedulers.get(schName);

		return scheduler;
	}

}
