/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: SchedulerQueuedTaskImpl
 * Wanglei 2011-4-20
 */
package dyna.app.server.core.sch;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * 异步线程调度
 * @author Lizw
 * 
 */
public class AsyncSchedulerImpl implements AsyncScheduler
{
	private ExecutorService queueTaskExecutor = null;

	/**
	 * @param threadSize
	 */
	public AsyncSchedulerImpl(int threadSize)
	{
		if (threadSize < 1)
		{
			threadSize = DEFAULT_THREAD_SIZE;
		}

		// 使用具有优先级的无界队列PriorityBlockingQueue构造线程池
		queueTaskExecutor = new ThreadPoolExecutor(threadSize, threadSize, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.core.sch.Scheduler#addTask(dyna.app.core.sch.ScheduledTask)
	 */
	@Override
	public void addTask(PriorityScheduledTask task)
	{
		queueTaskExecutor.execute(task);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dyna.app.core.sch.Scheduler#clearTask()
	 */
	@Override
	public void clearTask()
	{
	}

	@Override
	public void submitTask(PriorityScheduledTask task)
	{
		queueTaskExecutor.submit(task);
	}

	@Override
	public void shutdown()
	{
		queueTaskExecutor.shutdown();
	}

}
