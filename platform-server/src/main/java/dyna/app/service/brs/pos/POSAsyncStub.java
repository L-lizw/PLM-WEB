package dyna.app.service.brs.pos;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.async.AsyncImpl;
import dyna.app.service.brs.emm.ClassStub;
import dyna.app.service.brs.emm.EMMImpl;
import dyna.app.service.brs.pos.POSImpl;
import dyna.common.SearchCondition;
import dyna.common.dto.BIViewHis;
import dyna.common.dto.Preference;
import dyna.common.dto.PreferenceDetail;
import dyna.common.dto.Search;
import dyna.common.dto.model.bmbo.BOInfo;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.PreferenceTypeEnum;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.POS;
import dyna.net.service.data.SystemDataService;
import org.acegisecurity.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lizw
 * @date 2022/1/28
 **/
@Component
public class POSAsyncStub extends AbstractServiceStub<POSImpl>
{
	protected void deleteHistory(String userGuid)
	{
		deleteBIViewHis(userGuid);
		deleteSearchHis(userGuid);
	}

	private void deleteBIViewHis(String userGuid) {
		try
		{
			Preference preference =  this.stubService.getPreference(PreferenceTypeEnum.BIVIEWHISCOUNT, userGuid);
			int maxRowNum = BIViewHis.MAXROWNUM;
			if (preference != null)
			{
				List<PreferenceDetail> preferenceDetailList = preference.getPreferenceDetailList();
				if (!SetUtils.isNullList(preferenceDetailList))
				{
					maxRowNum = Integer.parseInt(preferenceDetailList.get(0).getValue());
				}
			}

			SystemDataService sds = this.stubService.getSystemDataService();
			BIViewHis his = new BIViewHis();
			his.put(BIViewHis.CREATE_USER, userGuid);
			List<BIViewHis> hisListOfUser = sds.query(BIViewHis.class, his, "selectByUser");
			hisListOfUser.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
			if (hisListOfUser.size() > maxRowNum)
			{
				List<BIViewHis> deleteHisList = hisListOfUser.subList(maxRowNum, hisListOfUser.size());
				if (!SetUtils.isNullList(deleteHisList))
				{
					for (BIViewHis item : deleteHisList)
					{
						BIViewHis deleteHis = new BIViewHis();
						deleteHis.setInstanceGuid(item.getInstanceGuid());
						deleteHis.put(BIViewHis.CREATE_USER, item.get(BIViewHis.CREATE_USER));
						sds.delete(BIViewHis.class, deleteHis, "deleteBy");
					}
				}
			}
		}
		catch (Throwable e)
		{
			DynaLogger.error(e.getMessage(), e);
		}

	}

	private void deleteSearchHis(String userGuid)
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		int maxHistoryNum = 0;

		// 如果用户做了"最近打开"的数量的配置，那么以用户配置的为准
		// 如果用户没做配置，那么取系统默认的最大条数

		try
		{
			Preference preference = this.stubService.getPreference(PreferenceTypeEnum.MAXHISTORY, userGuid);
			maxHistoryNum =Search.MAX_HISTORY_NUM;
			if (preference != null)
			{
				List<PreferenceDetail> preferenceDetailList = preference.getPreferenceDetailList();
				if (!SetUtils.isNullList(preferenceDetailList))
				{
					maxHistoryNum = Integer.parseInt(preferenceDetailList.get(0).getValue());
				}
			}
			Map<String, Object> filter = new HashMap<String, Object>();
			filter.put(Search.OWNER_USER, userGuid);
			List<Search> searchList = sds.query(Search.class, filter);
			if (!SetUtils.isNullList(searchList))
			{
				for (int i = searchList.size() - 1; i > maxHistoryNum - 1; i--)
				{
					sds.delete(Search.class, searchList.get(i).getGuid());
				}
			}
		}
		catch (Throwable e)
		{
			DynaLogger.error(e.getMessage(), e);
		}
	}


	protected void saveSearchByCondition(SearchCondition condition, String searchGuid) throws ServiceRequestException
	{
		if (StringUtils.isGuid(searchGuid))
		{
			this.stubService.saveSearch(searchGuid, condition);
		}
		else
		{
			if (!SetUtils.isNullList(condition.getCriterionList()))
			{
				Search search = new Search();
				search.setIsAdvanced(condition.isAdvanced());
				String classGuid = null;
				String clasfGuid = null;
				String folderGuid = null;
				BOInfo boInfo = null;

				if (condition.getObjectGuid() != null && condition.getObjectGuid().getClassName() != null)
				{
					ClassStub.decorateObjectGuid(condition.getObjectGuid(), this.stubService);

					boInfo = ((EMMImpl) this.stubService.getEMM()).getBMStub().getCurrentBoInfoByClassName(condition.getObjectGuid().getClassName());
				}

				if (boInfo != null)
				{
					classGuid = boInfo.getClassGuid();
					clasfGuid = boInfo.getClassificationGuid();
				}

				if (condition.getFolder() != null)
				{
					folderGuid = condition.getFolder().getGuid();
				}

				this.stubService.getMySearchStub().saveSearch(search, classGuid, clasfGuid, folderGuid, condition);
			}
		}

	}

}
