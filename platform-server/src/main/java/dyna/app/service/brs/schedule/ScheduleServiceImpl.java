package dyna.app.service.brs.schedule;

import dyna.app.service.BusinessRuleService;
import dyna.common.log.DynaLogger;
import dyna.net.service.brs.*;
import dyna.net.service.das.MSRM;
import dyna.net.service.data.SystemDataService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author Lizw
 * @date 2022/1/29
 **/
@Service public class ScheduleServiceImpl extends BusinessRuleService implements ScheduleService
{
	@DubboReference private SystemDataService systemDataService;

	@Autowired private AAS  aas;
	@Autowired private FTS  fts;
	@Autowired private LIC  lic;
	@Autowired private MSRM msrm;
	@Autowired private PPMS ppms;
	@Autowired private SMS  sms;
	@Autowired private WFI  wfi;

	@Autowired protected ScheduleDSSStub  dssStub;
	@Autowired private   ScheduleFTSStub  ftsStub;
	@Autowired private   ScheduleLICStub  licStub;
	@Autowired private   SchedulePPMSStub ppmsStub;
	@Autowired private   ScheduleSMSStub  smsStub;
	@Autowired private   ScheduleWFIStub  wfiStub;

	protected SystemDataService getSystemDataService()
	{
		return this.systemDataService;
	}

	protected AAS getAAS()
	{
		return this.aas;
	}

	protected FTS getFTS()
	{
		return this.fts;
	}

	protected LIC getLic()
	{
		return this.lic;
	}

	protected MSRM getMSRM()
	{
		return this.msrm;
	}

	protected PPMS getPPMS()
	{
		return this.ppms;
	}

	protected SMS getSMS()
	{
		return this.sms;
	}

	protected WFI getWFI()
	{
		return this.wfi;
	}

	protected ScheduleDSSStub getDssStub()
	{
		return this.dssStub;
	}

	protected ScheduleFTSStub getFTSStub()
	{
		return this.ftsStub;
	}

	protected ScheduleLICStub getLICStub()
	{
		return this.licStub;
	}

	protected SchedulePPMSStub getPPMSStub()
	{
		return this.ppmsStub;
	}

	protected ScheduleSMSStub getSMSStub()
	{
		return this.smsStub;
	}

	protected ScheduleWFIStub getWFIStub()
	{
		return this.wfiStub;
	}

	@Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 1000) @Override public void checkSession()
	{
		this.getLICStub().checkSession();
	}

	@Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 60 * 1000) @Override public void checkTransformQueue()
	{
		DynaLogger.info("Transform Queue Scheduled [Class]TransformQueueScheduledTask , Scheduled Task Start...");
		this.getFTSStub().checkTransformQueue();
		DynaLogger.info("QueueCheck Scheduled [Class]TransformQueueScheduledTask , Scheduled Task End...");
	}

	@Scheduled(cron = "0 0 1 * * *") @Override public void clearMail()
	{
		DynaLogger.info("Mail clear Scheduled [Class]ClearMailScheduled , Scheduled Task Start...");
		this.getSMSStub().clearMail();
		DynaLogger.info("Mail clear Scheduled [Class]ClearMailScheduled , Scheduled Task End...");
	}

	@Scheduled(cron = "* * 0/8 * * *") @Override public void deleteDSSFileTrans()
	{
		this.getDssStub().deleteFileTrans();
	}

	@Override public void fileTransDelete()
	{

	}

	@Scheduled(cron = "0 0 2 * * *") @Override public void projectCalculate()
	{
		DynaLogger.info("PPMS Scheduled [Class]ProjectCalculateScheduled , Scheduled Task Start...");
		this.getPPMSStub().projectCalculate();
		DynaLogger.info("PPMS Scheduled [Class]ProjectCalculateScheduled , Scheduled Task End...");
	}

	@Scheduled(cron = "0 0 3 * * *") @Override public void projectWarning()
	{
		DynaLogger.info("PPMS Warning Scheduled [Class]WarningScheduledTask , Scheduled Task Start...");
		this.getPPMSStub().projectWarning();
		DynaLogger.info("PPMS Warning Scheduled [Class]WarningScheduledTask , Scheduled Task End...");
	}

	@Scheduled(cron = "0 0 1 * * *") @Override public void runNoticeAction4Workflow()
	{
		DynaLogger.info("WFE notice Scheduled [Class]NoticeScheduledTask , Scheduled Task Start...");
		this.getWFIStub().runNoticeAction4Workflow();
		DynaLogger.info("WFE notice Scheduled [Class]NoticeScheduledTask , Scheduled Task End...");
	}

	@Scheduled(cron = "0 0 1 * * *") @Override public void runOverTimeAction()
	{
		DynaLogger.info("WFE OverTime Scheduled [Class]OverTimeActionScheduledTask , Scheduled Task Start...");
		this.getWFIStub().runOverTimeAction();
		DynaLogger.info("WFE OverTime Scheduled [Class]OverTimeActionScheduledTask , Scheduled Task End...");
	}

	@Scheduled(cron = "0 0 1 * * *") @Override public void runOverTimeAgent()
	{
		DynaLogger.info("WFE OverTime Scheduled [Class]OverTimeAgentScheduledTask , Scheduled Task Start...");
		this.getWFIStub().runOverTimeAgent();
		DynaLogger.info("WFE OverTime Scheduled [Class]OverTimeAgentScheduledTask , Scheduled Task End...");
	}
}
