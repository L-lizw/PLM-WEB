/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: JSSImpl Job Serialized Service 实现
 * Wanglei 2011-11-7
 */
package dyna.app.service.das.jss;

import dyna.app.conf.AsyncConfig;
import dyna.app.conf.yml.ConfigurableJSSImpl;
import dyna.app.conf.yml.ConfigurableServerImpl;
import dyna.app.conf.yml.ConfigurableServiceImpl;
import dyna.app.core.sch.AsyncSchedulerImpl;
import dyna.app.service.DataAccessService;
import dyna.app.service.brs.async.JSSAsyncStub;
import dyna.common.conf.*;
import dyna.common.dto.Queue;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.JobGroupEnum;
import dyna.common.systemenum.JobStatus;
import dyna.net.service.brs.*;
import dyna.net.service.das.JSS;
import dyna.net.service.das.MSRM;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.SystemDataService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Job Serialized Service 实现
 *
 * @author Wanglei
 */
@Service public class JSSImpl extends DataAccessService implements JSS
{
	private static boolean initialized = false;
	private static boolean runJobQuery = true;

	@DubboReference private DSCommonService   dsCommonService;
	@DubboReference private SystemDataService systemDataService;

	@Autowired private Async async;
	@Autowired private ERPI  erpi;
	@Autowired private MSRM  msrm;
	@Autowired private SMS   sms;

	@Autowired private JobQueryStub            jobQueryStub;
	@Autowired private JobCreationStub         jobCreationStub;
	@Autowired private JobUpdaterStub          jobUpdaterStub;
	@Autowired private AsyncConfig             asyncConfig;
	@Autowired private ConfigurableServerImpl  serverConfig;
	@Autowired private ConfigurableJSSImpl     configurableJSS;

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.service.DataAccessService#init()
	 */
	@Override public void init(ServiceDefinition serviceDefinition)
	{
		super.init(serviceDefinition);
		String enable = serviceDefinition.getParam().get("enable");
		runJobQuery = !"false".equalsIgnoreCase(enable);
		syncInit();
	}

	private synchronized void syncInit()
	{
		// add queued task scheduler

		if (runJobQuery)
		{
			if (configurableJSS.getSchedulerMap() != null)
			{
				Iterator<SchedulerDefinition> itr = configurableJSS.getSchedulerMap().values().iterator();
				while (itr.hasNext())
				{
					SchedulerDefinition sd = itr.next();
					if (this.asyncConfig.getScheduler(sd.getId()) == null)
					{
						this.asyncConfig.addScheduler(sd.getId(), new AsyncSchedulerImpl(sd.getThreadCount()));
					}
				}
			}
		}
	}

	protected DSCommonService getDsCommonService()
	{
		return this.dsCommonService;
	}

	protected SystemDataService getSystemDataService()
	{
		return this.systemDataService;
	}

	protected Async getAsync()
	{
		return this.async;
	}

	protected ERPI getERPI()
	{
		return this.erpi;
	}

	protected MSRM getMSRM()
	{
		return this.msrm;
	}

	protected SMS getSMS()
	{
		return this.sms;
	}


	/**
	 * @return the jobQueryStub
	 */
	protected JobQueryStub getJobQueryStub() throws ServiceRequestException
	{
		return this.jobQueryStub;
	}

	protected JobCreationStub getJobCreationStub()
	{
		return this.jobCreationStub;
	}

	/**
	 * @return the jobUpdaterStub
	 */
	protected JobUpdaterStub getJobUpdaterStub()
	{
		return this.jobUpdaterStub;
	}

	protected synchronized BOAS getBOAS() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(BOAS.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	public synchronized AAS getAAS() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(AAS.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	public synchronized LIC getLIC() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(LIC.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	protected synchronized EMM getEMM() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(EMM.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#getJob(java.lang.String)
	 */
	@Override public Queue getJob(String jobObjectGuid) throws ServiceRequestException
	{
		return this.getJobQueryStub().getJob(jobObjectGuid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#listJob(dyna.common.SearchCondition)
	 */
	@Override public List<Queue> listJob(Map<String, Object> condition) throws ServiceRequestException
	{
		return this.getJobQueryStub().listJob(condition);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#listWaitingJob(dyna.common.SearchCondition)
	 */
	@Override public List<Queue> listWaitingJob(Map<String, Object> condition) throws ServiceRequestException
	{
		return this.getJobQueryStub().listWaitingJob(condition);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#listRunningJob(dyna.common.SearchCondition)
	 */
	@Override public List<Queue> listRunningJob(Map<String, Object> condition) throws ServiceRequestException
	{
		return this.getJobQueryStub().listRunningJob(condition);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#listCancelJob(dyna.common.SearchCondition)
	 */
	@Override public List<Queue> listCancelJob(Map<String, Object> condition) throws ServiceRequestException
	{
		return this.getJobQueryStub().listCancelJob(condition);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#listSuccessfulJob(dyna.common.SearchCondition)
	 */
	@Override public List<Queue> listSuccessfulJob(Map<String, Object> condition) throws ServiceRequestException
	{
		return this.getJobQueryStub().listSuccessfulJob(condition);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#listFailedJob(dyna.common.SearchCondition)
	 */
	@Override public List<Queue> listFailedJob(Map<String, Object> condition) throws ServiceRequestException
	{
		return this.getJobQueryStub().listFailedJob(condition);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#createJob(dyna.common.bean.data.FoundationObject)
	 */
	@Override public Queue createJob(Queue fo) throws ServiceRequestException
	{

		return this.getJobCreationStub().createJob(fo);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#saveJob(dyna.common.bean.data.FoundationObject)
	 */
	@Override public Queue saveJob(Queue fo) throws ServiceRequestException
	{
		return this.getJobUpdaterStub().saveJob(fo, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#setJobStatus(dyna.common.bean.data.FoundationObject,
	 * dyna.common.systemenum.JobStatus)
	 */
	@Override public Queue setJobStatus(Queue fo, JobStatus jobStatus) throws ServiceRequestException
	{
		return this.getJobUpdaterStub().setJobStatus(fo, jobStatus);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#deleteJobs(java.lang.String[])
	 */
	@Override public void deleteJobs(String... jobGuids) throws ServiceRequestException
	{
		this.getJobCreationStub().deleteJobs(jobGuids);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.JSS#deleteTimeoutJobs(java.lang.String, int)
	 */
	@Override public void deleteTimeoutJobs(String jobType, int timeOut) throws ServiceRequestException
	{
		this.getJobCreationStub().deleteTimeoutJobs(jobType, timeOut);
	}

	/**
	 * @param executorClass
	 * @return
	 */
	public JobDefinition getJobDefinition(String executorClass)
	{
		return this.configurableJSS.getJobDelfinitionWithClassName(executorClass);
	}

	public JobDefinition getJobDefinitionByType(String jobType)
	{
		return this.configurableJSS.getJobDelfinition(jobType);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.das.JSS#getJobQueueTypeList()
	 */
	@Override public List<JobDefinition> getJobQueueTypeList() throws ServiceRequestException
	{
		return configurableJSS.getJobDefinitionList();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.das.JSS#listNotFinishJobQueue(java.lang.String)
	 */
	@Override public List<Queue> listJob(String typeId, boolean isSearchOwner, List<JobStatus> statuslist) throws ServiceRequestException
	{
		return this.getJobQueryStub().listJob(typeId, isSearchOwner, statuslist);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.das.JSS#listJobQueue(java.util.Map, int, int, boolean)
	 */
	@Override public List<Queue> listJobQueue(Map<String, Object> searchCondition, int pageNum, int pageSize) throws ServiceRequestException
	{
		return this.getJobQueryStub().listJobQueue(searchCondition, pageNum, pageSize);
	}

	@Override public Queue saveJob4ERPNotify(Queue fo) throws ServiceRequestException
	{
		return this.getJobUpdaterStub().saveJob(fo, true);
	}

	@Override public void reStartQueue(Queue queue) throws ServiceRequestException
	{
		Queue newQueue = new Queue();
		queue.setGuid(null);
		newQueue = (Queue) queue.clone();
		if (queue.getJobGroup().equals(JobGroupEnum.ERP))
		{
			newQueue.setFieldh(String.valueOf(System.nanoTime()));
			newQueue.setFieldl(null);
		}
		this.createJob(newQueue);
	}

}
