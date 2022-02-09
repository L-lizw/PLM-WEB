package dyna.app.service.brs.bom;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.async.AsyncImpl;
import dyna.app.service.brs.boas.BOASImpl;
import dyna.app.service.brs.bom.BOMSImpl;
import dyna.app.service.brs.brm.BRMImpl;
import dyna.common.SearchCondition;
import dyna.common.SearchConditionFactory;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.StructureObject;
import dyna.common.bean.data.structure.BOMStructure;
import dyna.common.bean.sync.ListBOMTask;
import dyna.common.dto.DataRule;
import dyna.common.dto.model.cls.ClassInfo;
import dyna.common.dto.template.bom.BOMTemplateInfo;
import dyna.common.dto.template.relation.RelationTemplateInfo;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.ModelInterfaceEnum;
import dyna.common.systemenum.ReplaceRangeEnum;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.BOAS;
import dyna.net.service.brs.BOMS;
import dyna.net.service.brs.BRM;
import org.acegisecurity.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Lizw
 * @date 2022/1/27
 **/
@Component
public class BOMAsyncStub extends AbstractServiceStub<BOMSImpl>
{

	protected void updateUHasBOM(List<FoundationObject> end1List, String bmGuid) throws ServiceRequestException
	{
		if (!SetUtils.isNullList(end1List))
		{
			for (FoundationObject foundationObject : end1List)
			{
				List<BOMTemplateInfo> bomTemplateList = this.stubService.getEMM().listBOMTemplateByEND1(foundationObject.getObjectGuid(), bmGuid);
				if (!SetUtils.isNullList(bomTemplateList))
				{
					for (BOMTemplateInfo bomTemplate : bomTemplateList)
					{
						this.stubService.getRelationService().updateUHasBOM(foundationObject.getObjectGuid(), bomTemplate.getGuid(), foundationObject.getName(), this.stubService.getFixedTransactionId());
					}
				}
			}
		}
	}

	protected CompletableFuture<List<ObjectGuid>> CheckConnect(ObjectGuid end1, String templateName, boolean isBom)
	{
		List<ObjectGuid> end2ObjectGuidList = new LinkedList<ObjectGuid>();

		try
		{
			String templateId = null;
			String viewClassNameOrGuid = null;
			String struClassGuid = null;
			if (isBom)
			{
				BOMTemplateInfo template = this.stubService.getEMM().getBOMTemplateByName(end1, templateName);
				if (template == null)
				{
					return CompletableFuture.completedFuture(end2ObjectGuidList);
				}
				templateId = template.getId();
				struClassGuid = template.getStructureClassGuid();
				viewClassNameOrGuid = template.getViewClassGuid();
			}
			else
			{
				RelationTemplateInfo template = this.stubService.getEMM().getRelationTemplateByName(end1, templateName);
				if (template == null)
				{
					return CompletableFuture.completedFuture(end2ObjectGuidList);
				}
				templateId = template.getId();
				struClassGuid = template.getStructureClassGuid();
				viewClassNameOrGuid = template.getViewClassGuid();
			}
			end1.setIsMaster(true);
			//todo
			List<StructureObject> list = this.stubService.getRelationService().listSimpleStructureOfEnd1(end1, templateId, viewClassNameOrGuid, struClassGuid, null);
			if (list != null)
			{
				for (StructureObject end2 : list)
				{
					end2ObjectGuidList.add(end2.getEnd2ObjectGuid());
				}
			}

		}
		catch (ServiceRequestException e)
		{
			DynaLogger.debug(e.getMessage(), e);
		}

		return CompletableFuture.completedFuture(end2ObjectGuidList);

	}

	/**
	 * 根据主件或者元件或者取替代件，删除其取替代关系
	 */
	protected void deleteReplaceData(ObjectGuid objectGuid, String exceptionParameter, List<FoundationObject> end1List, String bmGuid, boolean deleteReplace)
	{
		try
		{
			this.stubService.getBOAS().deleteReference(objectGuid, exceptionParameter);
			if (!SetUtils.isNullList(end1List))
			{
				this.updateUHasBOM(end1List, bmGuid);
			}
			if (deleteReplace)
			{
				this.stubService.getBRM().deleteReplaceDataByItem(objectGuid);
			}
		}
		catch (Throwable e)
		{
			DynaLogger.error("run send mail:", e);
		}
	}

	protected CompletableFuture<ListBOMTask> listBOM(ObjectGuid end1, String templateName, SearchCondition searchCondition, SearchCondition end2SearchCondition,
			DataRule dataRule, int level)
	{
		ListBOMTask listBOMTask = new ListBOMTask();
		try
		{
			listBOMTask.setLevel(level);
			List<BOMStructure> bomStruList = this.stubService.listBOM(end1, templateName, searchCondition, end2SearchCondition, dataRule);
			listBOMTask.setBomList(bomStruList);
			listBOMTask.setEnd1guid(end1.getGuid());
		}
		catch (ServiceRequestException e)
		{
			DynaLogger.debug(e.getMessage(), e);
		}

		return CompletableFuture.completedFuture(listBOMTask);

	}

	protected CompletableFuture<ListBOMTask> listBOMForAllTemplate(ObjectGuid end1, SearchCondition end2SearchCondition, DataRule dataRule)
	{
		ListBOMTask listBOMTask = new ListBOMTask();
		try
		{
			List<BOMStructure> bomStruList = new ArrayList<>();
			List<BOMTemplateInfo> bomtempList = this.stubService.getEMM().listBOMTemplateByEND1(end1);
			if (bomtempList != null)
			{
				for (BOMTemplateInfo info : bomtempList)
				{
					SearchCondition createSearchCondition4Class = SearchConditionFactory.createSearchCondition4Class(info.getStructureClassName(), null, true);
					List<BOMStructure> tempbomStruList = this.stubService.listBOM(end1, info.getName(), createSearchCondition4Class, end2SearchCondition, dataRule);
					if (tempbomStruList != null)
					{
						bomStruList.addAll(tempbomStruList);
					}
				}
			}
			listBOMTask.setBomList(bomStruList);
		}
		catch (ServiceRequestException e)
		{
			DynaLogger.error(e.getMessage(), e);
		}

		return CompletableFuture.completedFuture(listBOMTask);
	}
}
