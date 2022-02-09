package dyna.app.service.brs.schedule;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.ppms.PPMSImpl;
import dyna.common.SearchCondition;
import dyna.common.SearchConditionFactory;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ppms.PPMFoundationObjectUtil;
import dyna.common.dto.model.bmbo.BOInfo;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.SearchRevisionTypeEnum;
import dyna.common.util.SetUtils;
import dyna.net.service.brs.PPMS;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目，工作项定时任务详细执行
 *
 * @author Lizw
 * @date 2022/1/29
 **/
@Component
public class SchedulePPMSStub extends AbstractServiceStub<ScheduleServiceImpl>
{
	protected  void projectCalculate()
	{
		try
		{
			this.calculateProjectSPI();
			this.calculateWorkItem();
		}
		catch (Throwable e)
		{
			DynaLogger.error("run ProjectCalculateScheduled:", e);
		}
	}

	/**
	 * 计算项目信息
	 * 项目定时任务
	 *
	 * @throws ServiceRequestException
	 */
	private void calculateProjectSPI() throws ServiceRequestException
	{
		BOInfo pmProjectBoInfo = this.stubService.getPPMS().getPMProjectBoInfo();
		if (pmProjectBoInfo == null)
		{
			return;
		}
		SearchCondition searchCondition = SearchConditionFactory.createSearchCondition4Class(pmProjectBoInfo.getClassName(), null, true);
		searchCondition.setPageSize(SearchCondition.MAX_PAGE_SIZE);
		searchCondition.setPageNum(1);
		searchCondition.setSearchRevisionTypeEnum(SearchRevisionTypeEnum.ISLATESTONLY);
		List<FoundationObject> listProject = this.stubService.getPPMS().listProject(searchCondition);

		if (!SetUtils.isNullList(listProject))
		{
			int size = listProject.get(0).getRowCount();
			size = size / SearchCondition.MAX_PAGE_SIZE;
			for (int i = 1; i <= size + 1; i++)
			{
				if (i > 1)
				{
					searchCondition.setPageNum(i);
					listProject = this.stubService.getPPMS().listProject(searchCondition);
				}
				if (!SetUtils.isNullList(listProject))
				{
					for (FoundationObject project : listProject)
					{
						this.stubService.getPPMS().calculateProjectInfo(project);
					}
				}
			}

		}
	}

	/**
	 * 工作项定时任务
	 *
	 * @throws ServiceRequestException
	 */
	private void calculateWorkItem() throws ServiceRequestException
	{
		BOInfo pmWorkBoInfo = this.stubService.getPPMS().getWorkItemBoinfo();
		if (pmWorkBoInfo == null)
		{
			return;
		}
		SearchCondition searchCondition = SearchConditionFactory.createSearchCondition4Class(pmWorkBoInfo.getClassName(), null, true);
		searchCondition.setPageSize(SearchCondition.MAX_PAGE_SIZE);
		searchCondition.setSearchRevisionTypeEnum(SearchRevisionTypeEnum.ISLATESTONLY);
		searchCondition.setPageNum(1);
		List<FoundationObject> listWork = this.stubService.getPPMS().listWorkItem(searchCondition);
		if (!SetUtils.isNullList(listWork))
		{
			int size = listWork.get(0).getRowCount();
			size = size / SearchCondition.MAX_PAGE_SIZE;
			for (int i = 1; i <= size + 1; i++)
			{
				if (i > 1)
				{
					searchCondition.setPageNum(i);
					listWork = this.stubService.getPPMS().listWorkItem(searchCondition);
				}
				if (!SetUtils.isNullList(listWork))
				{
					for (FoundationObject work : listWork)
					{
						PPMFoundationObjectUtil util = new PPMFoundationObjectUtil(work);
						((PPMSImpl) this.stubService.getPPMS()).getWorkItemStub().calculateSPIAndDuration(util);
						((PPMSImpl) this.stubService.getPPMS()).getProjectStub().saveObject(work, false, false);
					}
				}
			}
		}
	}

	protected void projectWarning()
	{
		try
		{
			this.stubService.getPPMS().dispatchProjectWarningRule();
		}
		catch (Throwable e)
		{
			DynaLogger.error("run runWarningRule:", e);
		}

	}

}
