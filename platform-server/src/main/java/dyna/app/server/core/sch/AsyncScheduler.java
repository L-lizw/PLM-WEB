/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: Scheduler 计划任务调度器
 * Wanglei 2011-4-20
 */
package dyna.app.server.core.sch;

/**
 * 计划任务调度器
 * 
 * @author Lizw
 * 
 */
public interface AsyncScheduler
{
	public final static int	DEFAULT_THREAD_SIZE				= 16;
	public final static int	DEFAULT_SCHEDULED_THREAD_SIZE	= 5;
	public final static int	DEFAULT_DELAY					= 30;

	/**
	 * 添加计划任务, 系统将根据当前任务情况, 安排适当的执行时间
	 * 
	 * @param task
	 */
	public void addTask(PriorityScheduledTask task);

	/**
	 * 提交计划任务, 并且使用合适的线程立即执行.如果没有可用的线程, 将等待空闲线程再执行
	 * 
	 * @param task
	 */
	public void submitTask(PriorityScheduledTask task);

	/**
	 * 清除队列中的所有任务
	 */
	public void clearTask();

	/**
	 * 关闭调度器, 所有任务将不再执行, 调度器将不可用
	 */
	public void shutdown();
}
