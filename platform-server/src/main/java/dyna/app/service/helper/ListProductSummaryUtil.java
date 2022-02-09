package dyna.app.service.helper;

import dyna.common.SearchCondition;
import dyna.common.SearchConditionFactory;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.StructureObject;
import dyna.common.bean.data.structure.BOMStructure;
import dyna.common.bean.sync.ListBOMTask;
import dyna.common.dto.DataRule;
import dyna.common.dto.model.cls.ClassInfo;
import dyna.common.dto.model.code.CodeItemInfo;
import dyna.common.dto.template.relation.RelationTemplateInfo;
import dyna.common.exception.ServiceRequestException;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.Async;
import dyna.net.service.brs.BOAS;
import dyna.net.service.brs.BOMS;
import dyna.net.service.brs.EMM;
//import org.apache.ftpserver.util.RegularExpr;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ListProductSummaryUtil implements Comparator<FoundationObject>
{
	private BOAS                          boas                 = null;
	private BOMS                          boms                 = null;
	private EMM                           emm                  = null;
	private String                        assoTemplateName     = null;
	private SearchCondition               bomEnd2SearchCondition;
	private SearchCondition               assoSearchCondition;
	private SearchCondition               assoEnd2SearchCondition;
	private SearchCondition               summarySearchCondition;
	private DataRule                      dataRule;
	private Map<String, ObjectGuid>       allInstanceMasterMap = new HashMap<>();
	private Map<String, Object>           allRunTaskMap        = new HashMap<>();
	private Map<String, FoundationObject> returnMap            = new HashMap<>();
	private List<String>                  filterEnd2Class      = new LinkedList<>();
	private List<String>					filterEnd2Classification	= new LinkedList<>();

	public ListProductSummaryUtil(BOAS boas, BOMS boms, EMM  emm)
	{
		this.boas = boas;
		this.boms = boms;
		this.emm =emm;
	}

	public List<FoundationObject> listProductSummaryObject(ObjectGuid end1, String templateName, SearchCondition searchCondition, DataRule dataRule) throws ServiceRequestException
	{
		this.assoTemplateName = templateName;
		this.summarySearchCondition = searchCondition;
		List<RelationTemplateInfo> assoTempList = this.emm.listRelationTemplateByName(templateName, false);
		if (SetUtils.isNullList(assoTempList))
		{
			return null;
		}
		assoSearchCondition = SearchConditionFactory.createSearchCondition4Class(assoTempList.get(0).getStructureClassName(), null, true);
		assoEnd2SearchCondition = summarySearchCondition;
		ClassInfo firstLevelClassByItem = this.emm.getFirstLevelClassByItem();
		if (firstLevelClassByItem == null)
		{
			return null;
		}
		String itemClassName = firstLevelClassByItem.getName();
		bomEnd2SearchCondition = SearchConditionFactory.createSearchCondition4Class(itemClassName, null, true);
		this.initResultFilter();
		this.dataRule = dataRule;
		this.add(end1);
		while (allRunTaskMap.size() > 0)
		{
			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			try
			{
				Map<String, Object> tempMap = new HashMap<>(allRunTaskMap);
				for (Entry<String, Object> entry : tempMap.entrySet())
				{
					if (! (entry.getValue() instanceof List))
					{
							if (((CompletableFuture<ListBOMTask>) entry.getValue()).isDone())
							{
								allRunTaskMap.remove(entry.getKey());
								List<BOMStructure> end2List = ((CompletableFuture<ListBOMTask>) entry.getValue()).get().getBomList();
								if (end2List != null)
								{
									for (BOMStructure end2 : end2List)
									{
										this.add(end2.getEnd2ObjectGuid());
									}
								}
					}
					else
					{
						if (((CompletableFuture<List<StructureObject>>) entry.getValue()).isDone())
						{
							allRunTaskMap.remove(entry.getKey());
							List<StructureObject> end2List = ((CompletableFuture<List<StructureObject>>) entry.getValue()).get();
							if (end2List != null)
							{
								for (StructureObject end2 : end2List)
								{
									this.processsResult(end2.getEnd2UIObject());
								}
							}
						}

					}
				}
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		}

		ArrayList<FoundationObject> arrayList = new ArrayList<>(returnMap.values());
		List<Map<String, Boolean>> orderMapList = this.summarySearchCondition.getOrderMapList();
		if (!SetUtils.isNullList(orderMapList))
			;
		{
			Collections.sort(arrayList, this);
		}

		return arrayList;
	}

	@Override
	public int compare(FoundationObject o1, FoundationObject o2)
	{
		if (o1 == null)
		{
			return o2 == null ? 0 : -1;
		}
		if (o2 == null)
		{
			return 1;
		}
		List<Map<String, Boolean>> orderMapList = this.summarySearchCondition.getOrderMapList();
		if (!SetUtils.isNullList(orderMapList))
		{
			for (Map<String, Boolean> sortb : orderMapList)
			{
				if (!sortb.isEmpty())
				{
					String fieldName = sortb.keySet().iterator().next();
					String value1 = (String) o1.get(fieldName);
					String value2 = (String) o1.get(fieldName);
					if (value1 == null)
					{
						if (value2 != null)
						{
							return -1;
						}
					}
					else if (value2 == null)
					{
						return 1;
					}
					else
					{
						int value = value1.compareTo(value2);
						if (value != 0)
						{
							return value;
						}
					}
				}
			}
		}
		return 0;
	}

	private void initResultFilter() throws ServiceRequestException
	{
		if (!StringUtils.isNullString(this.summarySearchCondition.getObjectGuid().getClassName()))
		{
			List<ClassInfo> list = emm.listAllSubClassInfoOnlyLeaf(null, this.summarySearchCondition.getObjectGuid().getClassName());
			if (list != null)
			{
				for (ClassInfo info : list)
				{
					this.filterEnd2Class.add(info.getGuid());
				}
			}
		}
		if (!StringUtils.isNullString(this.summarySearchCondition.getClassification()))
		{
			List<CodeItemInfo> list = emm.listLeafCodeItemInfoByDatail(this.summarySearchCondition.getClassification());
			if (list != null)
			{
				for (CodeItemInfo info : list)
				{
					this.filterEnd2Classification.add(info.getGuid());
				}
			}
		}

	}

	private void processsResult(FoundationObject end2uiObject)
	{
		if (!SetUtils.isNullList(filterEnd2Class) && filterEnd2Class.contains(end2uiObject.getObjectGuid().getClassGuid()) == false)
		{
			return;
		}
		if (!SetUtils.isNullList(filterEnd2Classification) && filterEnd2Classification.contains(end2uiObject.getClassificationGuid()) == false)
		{
			return;
		}

		this.returnMap.put(end2uiObject.getObjectGuid().getGuid(), end2uiObject);
	}

	private void add(ObjectGuid og)
	{
		if (!allInstanceMasterMap.containsKey(og.getGuid()))
		{
			allInstanceMasterMap.put(og.getGuid(), og);
			CompletableFuture<ListBOMTask> listBOMTaskCompletableFuture = this.boms.listBOMForAllTemplate(og, bomEnd2SearchCondition, dataRule);
			allRunTaskMap.put(og.getMasterGuid(), listBOMTaskCompletableFuture);
			CompletableFuture<List<StructureObject>> structureObjectList = this.boas
					.listObjectOfRelationAsync(og, assoTemplateName, assoSearchCondition, assoEnd2SearchCondition, dataRule);
			allRunTaskMap.put(og.getGuid(), structureObjectList);
		}
	}

}
