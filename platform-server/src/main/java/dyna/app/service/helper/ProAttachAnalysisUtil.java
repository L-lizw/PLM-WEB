package dyna.app.service.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.sync.AnalysisTask;
import dyna.common.dto.template.wft.WorkflowTemplateScopeRTInfo;
import dyna.common.dto.wf.ProcAttachSetting;
import dyna.common.dto.wf.WFRelationSet;
import dyna.common.util.SetUtils;
import dyna.net.service.brs.BOAS;

public class ProAttachAnalysisUtil
{
	private String								credential		= null;

	private boolean								checkAcl		= false;

	private BOAS boas = null;

	private Map<String, CompletableFuture<AnalysisTask>>			allRunTaskMap	= new HashMap<>();

	private Set<String>							distinctSet		= new HashSet<>();

	private List<FoundationObject>				returnList		= new LinkedList<>();

	private List<WorkflowTemplateScopeRTInfo>	listScopeRT		= null;

	public ProAttachAnalysisUtil(String credential2, BOAS boas)
	{
		this.credential = credential2;
		this.boas = boas;
	}

	public List<FoundationObject> calculateAttach(List<ObjectGuid> firstList, ProcAttachSetting settings, List<WorkflowTemplateScopeRTInfo> listScopeRT, boolean isCheckAcl)
	{
		this.checkAcl = isCheckAcl;

		this.listScopeRT = listScopeRT;

		boolean listBomAll = false;

		boolean listRelationAll = false;

		if (settings != null && settings.getRelationSetList() != null)
		{
			for (WFRelationSet info : settings.getRelationSetList())
			{
				if (info.isBOM())
				{
					listBomAll = (!"1".equals(info.getStrategy()));
				}
				else
				{
					listRelationAll = (!"1".equals(info.getStrategy()));
				}
			}
		}
		for (ObjectGuid og : firstList)
		{
			addTask(og, null, true, true);
		}
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
			Map<String, CompletableFuture<AnalysisTask>> tempMap = new HashMap<>(allRunTaskMap);
			for (Entry<String, CompletableFuture<AnalysisTask>> entry : tempMap.entrySet())
			{
				if (entry.getValue().isDone())
				{
					allRunTaskMap.remove(entry.getKey());
					List<FoundationObject> end2List = null;
					try
					{
						end2List = entry.getValue().get().getBomEnd2List();

					if (end2List != null)
					{
						for (FoundationObject end2 : end2List)
						{
							this.addTask(end2.getObjectGuid(), end2, listBomAll, true);
						}
					}

					end2List = entry.getValue().get().getAssoEnd2List();
					if (end2List != null)
					{
						for (FoundationObject end2 : end2List)
						{
							this.addTask(end2.getObjectGuid(), end2, listBomAll, listRelationAll);
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
			}
		}
		return returnList;
	}

	private void addTask(ObjectGuid og, FoundationObject obj, boolean listBom, boolean listRealation)
	{
		if (distinctSet.contains(og.getGuid()) == false)
		{
			distinctSet.add(og.getGuid());
			if (obj != null)
			{
				returnList.add(obj);
			}

			if (listBom || (listRealation && !SetUtils.isNullList(listScopeRT)))
			{
				CompletableFuture<AnalysisTask> completableFuture = this.boas.listBOMAndRelation(og, checkAcl,listBom, listRealation, listScopeRT);
				allRunTaskMap.put(og.getGuid(), completableFuture);
			}
		}
	}
}
