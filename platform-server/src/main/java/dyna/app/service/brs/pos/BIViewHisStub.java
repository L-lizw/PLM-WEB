/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: BIViewHisStub
 * Caogc 2010-8-18
 */
package dyna.app.service.brs.pos;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.helper.ServiceRequestExceptionWrap;
import dyna.app.service.helper.decorate.DecoratorFactory;
import dyna.common.SearchCondition;
import dyna.common.bean.data.FoundationObject;
import dyna.common.dto.BIViewHis;
import dyna.common.exception.DecorateException;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.SystemClassFieldEnum;
import dyna.common.util.DateFormat;
import dyna.common.util.SetUtils;
import dyna.net.service.brs.BOAS;
import dyna.net.service.data.DSCommonService;
import dyna.net.service.data.SystemDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 与最近访问的对象的历史相关的操作分支
 *
 * @author Lizw
 */
@Component
public class BIViewHisStub extends AbstractServiceStub<POSImpl>
{

	@Autowired
	private DecoratorFactory decoratorFactory;

	protected List<FoundationObject> listBIViewHis(final SearchCondition searchCondition) throws ServiceRequestException
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		DSCommonService ds = this.stubService.getDSCommonService();

		Map<String, Object> filter = new HashMap<String, Object>();// FilterBuilder.buildFilterBySearchCondition(searchCondition);

		try
		{
			String operatorGuid = this.stubService.getOperatorGuid();
			List<FoundationObject> foundationObjectList = new ArrayList<FoundationObject>();

			Map<String, Object> searchConditionMap = new HashMap<String, Object>();
			searchConditionMap.put("GROUPGUID", this.stubService.getUserSignature().getLoginGroupGuid());
			searchConditionMap.put(BIViewHis.CREATE_USER, operatorGuid);
			List<BIViewHis> bomViewHistoryList = sds.query(BIViewHis.class, searchConditionMap, "selectClassOfHistory");
			if (!SetUtils.isNullList(bomViewHistoryList))
			{
				for (BIViewHis bomViewHis : bomViewHistoryList)
				{
					filter.put(BIViewHis.CREATE_USER, operatorGuid);
					filter.put("GROUPGUID", this.stubService.getUserSignature().getLoginGroupGuid());
					filter.put("CLASSGUID", bomViewHis.getInstanceClassGuid());
					filter.put("tablename", ds.getTableName(bomViewHis.getInstanceClassGuid()));
					List<FoundationObject> tempList = sds.query(FoundationObject.class, filter, "selectHistoryView");
					if (!SetUtils.isNullList(tempList))
					{
						foundationObjectList.addAll(tempList);
					}
				}
			}

			List<Map<String, Boolean>> orderMapList = searchCondition.getOrderMapList();
			if (SetUtils.isNullList(orderMapList))
			{
				orderMapList = new ArrayList<Map<String, Boolean>>();

				Map<String, Boolean> orderMap = new HashMap<String, Boolean>();
				orderMap.put(SystemClassFieldEnum.CREATETIME.getName(), false);
				orderMapList.add(orderMap);
			}
			this.sortFoundationList(foundationObjectList, orderMapList);

			Set<String> fieldNames = new HashSet<String>();
			fieldNames.add(SystemClassFieldEnum.CLASSIFICATION.getName());
			fieldNames.add(SystemClassFieldEnum.CHECKOUTUSER.getName());
			fieldNames.add(SystemClassFieldEnum.OBSOLETEUSER.getName());

			for (FoundationObject foundationObject : foundationObjectList)
			{
				decoratorFactory.decorateFoundationObject(fieldNames, foundationObject, this.stubService.getEmm(), this.stubService.getUserSignature().getLoginGroupBMGuid(), null);
				decoratorFactory.decorateFoundationObjectCode(fieldNames, foundationObject, this.stubService.getEmm(), this.stubService.getUserSignature().getLoginGroupBMGuid());
			}

			decoratorFactory.decorateFoundationObject(fieldNames, foundationObjectList, this.stubService.getEmm(), this.stubService.getSignature().getCredential());
			return foundationObjectList;
		}
		catch (DynaDataException e)
		{
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
		catch (DecorateException e)
		{
			throw ServiceRequestException.createByDecorateException(e);
		}
	}

	private void sortFoundationList(List<FoundationObject> list, final List<Map<String, Boolean>> orderMapList)
	{
		Collections.sort(list, new Comparator<FoundationObject>()
		{

			@Override
			public int compare(FoundationObject o1, FoundationObject o2)
			{
				for (Map<String, Boolean> orderMap : orderMapList)
				{
					for (String fieldName : orderMap.keySet())
					{
						Object orig = o1.get(fieldName.toUpperCase());
						Object dest = o2.get(fieldName.toUpperCase());

						if ("bo".equalsIgnoreCase(fieldName))
						{
							orig = o1.get("BOGUID$");
							dest = o2.get("BOGUID$");
						}
						else if (SystemClassFieldEnum.getFoundationSystemClassFieldList().contains(fieldName.toUpperCase() + "$"))
						{
							orig = o1.get(fieldName.toUpperCase() + "$");
							dest = o2.get(fieldName.toUpperCase() + "$");
						}

						// 都为空，比较下一个排序字段
						if (orig == null && dest == null)
						{
							continue;
						}

						Boolean type = orderMap.get(fieldName);
						if (type == null)
						{
							type = true;
						}

						// null值在排序中为最大
						if (orig == null)
						{
							// 升序
							if (type)
							{
								return 1;
							}
							// 降序
							else
							{
								return -1;
							}
						}
						else if (dest == null)
						{
							// 升序
							if (type)
							{
								return -1;
							}
							// 降序
							else
							{
								return 1;
							}
						}
						else
						{
							if (orig.equals(dest))
							{
								continue;
							}
							else
							{
								if (orig instanceof Date)
								{
									if (type)
									{
										return DateFormat.compareDate((Date) orig, (Date) dest, DateFormat.PTN_YMDHMS);
									}
									else
									{
										return DateFormat.compareDate((Date) dest, (Date) orig, DateFormat.PTN_YMDHMS);
									}
								}
								else if (orig instanceof Number)
								{
									if (type)
									{
										if (((Number) orig).doubleValue() > ((Number) dest).doubleValue())
										{
											return 1;
										}
										else
										{
											return -1;
										}
									}
									else
									{
										if (((Number) dest).doubleValue() > ((Number) orig).doubleValue())
										{
											return 1;
										}
										else
										{
											return -1;
										}
									}
								}
								else
								{
									if (type)
									{
										return orig.toString().compareTo(dest.toString());
									}
									else
									{
										return dest.toString().compareTo(orig.toString());
									}
								}
							}
						}
					}
				}
				return 0;
			}
		});
	}

	protected void saveBIViewHis(BIViewHis biViewHis) throws ServiceRequestException
	{
		SystemDataService sds = this.stubService.getSystemDataService();

		try
		{

			String operatorGuid = this.stubService.getOperatorGuid();

			BIViewHis his = new BIViewHis();
			his.setInstanceGuid(biViewHis.getInstanceGuid());
			his.put(BIViewHis.CREATE_USER, operatorGuid);
			List<BIViewHis> tmpList = sds.query(BIViewHis.class, his, "selectByUserIns");
			if (!SetUtils.isNullList(tmpList))
			{
				sds.update(BIViewHis.class, his, "update");
			}
			else
			{
				sds.save(biViewHis);
			}
			this.stubService.getAsync().deleteHistory(operatorGuid);
		}
		catch (DynaDataException e)
		{
			throw ServiceRequestExceptionWrap.createByDynaDataException(this.stubService, e);
		}
	}
}
