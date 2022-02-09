package dyna.app.service.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import dyna.common.SearchCondition;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.structure.BOMStructure;
import dyna.common.bean.sync.ListBOMTask;
import dyna.common.dto.DataRule;
import dyna.net.service.brs.BOMS;

public class ListRelationTreeUtil
{
	private BOMS   boms         = null;
	private String templateName = null;
	SearchCondition							searchCondition;
	SearchCondition							end2SearchCondition;
	DataRule								dataRule;
	private Map<String, ObjectGuid>                     allInstanceMasterMap = new HashMap<>();
	private Map<String, CompletableFuture<ListBOMTask>> allRunTaskMap        = new HashMap<>();
	private Map<String, List<BOMStructure>>             returnMap            = new HashMap<String, List<BOMStructure>>();
	private int                                         level                = 0;

	public ListRelationTreeUtil(BOMS stubService )
	{
		this.boms = stubService;
	}

	public Map<String, List<BOMStructure>> listBOMForTree(ObjectGuid end1, String templateName, SearchCondition searchCondition, SearchCondition end2SearchCondition,
			DataRule dataRule, int level1)
	{
		this.level = level1;
		this.templateName = templateName;
		this.searchCondition = searchCondition;
		this.end2SearchCondition = end2SearchCondition;
		this.dataRule = dataRule;
		this.add(end1, 0);
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
			Map<String, CompletableFuture<ListBOMTask>> tempMap = new HashMap<>(allRunTaskMap);
			for (Entry<String, CompletableFuture<ListBOMTask>> entry : tempMap.entrySet())
			{
				if (entry.getValue().isDone())
				{
					allRunTaskMap.remove(entry.getKey());
					List<BOMStructure> end2List = null;
					try
					{
						end2List = entry.getValue().get().getBomList();

						returnMap.put(entry.getKey(), end2List);
						int level2 = entry.getValue().get().getLevel() +1 ;
						if (level < 0 || level2 < level)
						{
							if (end2List != null)
							{
								for (BOMStructure end2 : end2List)
								{
									this.add(end2.getEnd2ObjectGuid(), level2);
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
			}
		}
		return returnMap;
	}

	private void add(ObjectGuid og, int level)
	{
		if (!allInstanceMasterMap.containsKey(og.getGuid()))
		{
			allInstanceMasterMap.put(og.getGuid(), og);
			CompletableFuture<ListBOMTask> bomList = this.boms.listBOM(og, templateName, searchCondition,  end2SearchCondition, dataRule, level);
			allRunTaskMap.put(og.getMasterGuid(), bomList);
		}
	}

}
