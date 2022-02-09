package dyna.app.service.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import dyna.common.bean.data.ObjectGuid;
import dyna.net.service.brs.Async;
import dyna.net.service.brs.BOMS;

public class CheckConnectUtil
{
	private BOMS                                             boms                 = null;
	private String                                           templateName         = null;
	private boolean                                          isBom                = false;
	private String                                           checkMasterGuid      = null;
	private boolean                                          isConnnect           = false;
	private Map<String, ObjectGuid>                          allInstanceMasterMap = new HashMap<>();
	private Map<String, CompletableFuture<List<ObjectGuid>>> allRunTaskMap        = new HashMap<>();

	public CheckConnectUtil(BOMS boms, String templateName, boolean isBom)
	{
		this.boms = boms;
		this.templateName = templateName;
		this.isBom = isBom;
	}

	public boolean checkConntc(ObjectGuid og)
	{
		this.checkMasterGuid = "";
		this.add(og);
		this.checkMasterGuid = og.getMasterGuid();
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
			Map<String, CompletableFuture<List<ObjectGuid>>> tempMap = new HashMap<>(allRunTaskMap);
			for (Entry<String, CompletableFuture<List<ObjectGuid>>> entry : tempMap.entrySet())
			{
				if (entry.getValue().isDone())
				{
					allRunTaskMap.remove(entry.getKey());
					List<ObjectGuid> end2List = null;
					try
					{
						end2List = entry.getValue().get();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					catch (ExecutionException e)
					{
						e.printStackTrace();
					}
					if (!isConnnect)
					{
						if (end2List != null)
						{
							for (ObjectGuid end2 : end2List)
							{
								this.add(end2);
								if (isConnnect)
								{
									break;
								}
							}
						}
					}
				}
			}
		}
		return isConnnect;
	}

	private void add(ObjectGuid og)
	{
		if (!isConnnect)
		{
			if (checkMasterGuid.equals(og.getMasterGuid()))
			{
				isConnnect = true;
			}
			if (!isConnnect)
			{
				if (!allInstanceMasterMap.containsKey(og.getMasterGuid()))
				{
					allInstanceMasterMap.put(og.getMasterGuid(), og);
					CompletableFuture<List<ObjectGuid>> future = this.boms.checkConnect(og, templateName, isBom);
					allRunTaskMap.put(og.getMasterGuid(), future);
				}
			}
		}
	}
}
