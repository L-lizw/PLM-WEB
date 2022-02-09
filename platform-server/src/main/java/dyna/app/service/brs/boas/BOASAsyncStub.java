package dyna.app.service.brs.boas;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.async.AsyncImpl;
import dyna.app.service.brs.boas.BOASImpl;
import dyna.common.SearchCondition;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.StructureObject;
import dyna.common.bean.data.foundation.BOMView;
import dyna.common.bean.data.foundation.ViewObject;
import dyna.common.bean.data.structure.BOMStructure;
import dyna.common.bean.data.template.RelationTemplate;
import dyna.common.bean.sync.AnalysisTask;
import dyna.common.dto.DataRule;
import dyna.common.dto.template.bom.BOMTemplateInfo;
import dyna.common.dto.template.relation.RelationTemplateInfo;
import dyna.common.dto.template.wft.WorkflowTemplateScopeRTInfo;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.util.BooleanUtils;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Lizw
 * @date 2022/1/28
 **/
@Component
public class BOASAsyncStub extends AbstractServiceStub<BOASImpl>
{
	protected void updateHasEnd2Flg(ObjectGuid end1ObjectGuid, String relationTemplateGuid)
	{
		try
		{
			if (!StringUtils.isGuid(relationTemplateGuid))
			{
				return;
			}

			RelationTemplate relationTemplate = this.stubService.getEMM().getRelationTemplate(relationTemplateGuid);
			if (!relationTemplate.isIsRecordHasEnd2Data() || StringUtils.isNullString(relationTemplate.getFieldForRecordHasEnd2Data()))
			{
				return;
			}

			FoundationObject end1 = this.stubService.getObject(end1ObjectGuid);
			if (end1 == null)
			{
				throw new ServiceRequestException("ID_DS_NO_DATA", "end1 is not exist.");
			}

			boolean hasEnd2 = false;
			ViewObject viewObject = this.stubService.getRelationByEND1(end1ObjectGuid, relationTemplate.getName());
			if (viewObject != null)
			{
				List<FoundationObject> end2List = this.stubService.listFoundationObjectOfRelation(viewObject.getObjectGuid(), null, null, null, false);
				if (!SetUtils.isNullList(end2List))
				{
					hasEnd2 = true;
				}
			}

			end1.put(relationTemplate.getFieldForRecordHasEnd2Data(), null);
			end1.put(relationTemplate.getFieldForRecordHasEnd2Data(), BooleanUtils.getBooleanStringYN(hasEnd2));
			this.stubService.getFSaverStub().saveObject(end1, false, false, false, null, false, false, false);
		}
		catch (Throwable e)
		{
			DynaLogger.error("run send mail:", e);
		}

	}

	protected  void deleteReference(ObjectGuid objectGuid, String exceptionParameter)
	{
		try
		{
			this.stubService.deleteReference(objectGuid, exceptionParameter);
		}
		catch (Throwable e)
		{
			DynaLogger.error("run send mail:", e);
		}
	}

	protected CompletableFuture<List<StructureObject>>  listObjectOfRelation(ObjectGuid end1, String templateName, SearchCondition searchCondition, SearchCondition end2SearchCondition, DataRule dataRule)
	{
		try
		{
			List<StructureObject> structureObjectList = this.stubService.listObjectOfRelation(end1, templateName, searchCondition, end2SearchCondition, dataRule);
			return CompletableFuture.completedFuture(structureObjectList);
		}
		catch (ServiceRequestException e)
		{
			DynaLogger.error(e.getMessage(), e);
		}
		return CompletableFuture.completedFuture(new ArrayList<StructureObject>());
	}

	protected CompletableFuture<AnalysisTask> listBOMAndRelation(ObjectGuid objectGuid, boolean isCheckcl, boolean isbom, boolean isrelation, List<WorkflowTemplateScopeRTInfo> listScopeRT)
	{
		Set<String> distinctSet		= new HashSet<>();
		List<FoundationObject>		bomEnd2List		= new LinkedList<>();
		List<FoundationObject>				assoEnd2List	= new LinkedList<>();

		if (isbom)
		{
			listbom(objectGuid, distinctSet, bomEnd2List, isCheckcl);
		}
		if (isrelation)
		{
			listReation(objectGuid, listScopeRT, distinctSet, assoEnd2List, isCheckcl);
		}

		AnalysisTask analysisTask = new AnalysisTask();
		analysisTask.setBomEnd2List(bomEnd2List);
		analysisTask.setAssoEnd2List(assoEnd2List);
		return CompletableFuture.completedFuture(analysisTask);

	}


	private void listbom(ObjectGuid mainObjectGuid, Set<String> distinctSet, List<FoundationObject>	bomEnd2List, boolean isCheckAcl)
	{
		try
		{
			List<BOMTemplateInfo> bomTempLateList = this.stubService.getEMM().listBOMTemplateByEND1(mainObjectGuid);
			if (bomTempLateList != null)
			{
				for (BOMTemplateInfo bomTemplate : bomTempLateList)
				{
					BOMView viewObject = this.stubService.getBOMS().getBOMViewByEND1(mainObjectGuid, bomTemplate.getName());
					if (viewObject != null)
					{
						//todo
						List<BOMStructure> bomList = this.stubService.getRelationService().listBOMStructure(viewObject.getObjectGuid(), bomTemplate.getGuid(), null, null, isCheckAcl, null);
						if (!SetUtils.isNullList(bomList))
						{
							for (BOMStructure bomstru : bomList)
							{
								FoundationObject obj = (FoundationObject) bomstru.get(ViewObject.PREFIX_END2);
								if (bomstru.getEnd2ObjectGuid().getGuid() != null && !distinctSet.contains(bomstru.getEnd2ObjectGuid().getGuid()))
								{
									distinctSet.add(bomstru.getEnd2ObjectGuid().getGuid());
									bomEnd2List.add(obj);
								}
							}
						}
					}
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void listReation(ObjectGuid mainObjectGuid, List<WorkflowTemplateScopeRTInfo> listScopeRT, Set<String> distinctSet, List<FoundationObject>	assoEnd2List, boolean checkAcl)
	{
		if (!SetUtils.isNullList(listScopeRT))
		{
			for (WorkflowTemplateScopeRTInfo rt : listScopeRT)
			{
				try
				{
					RelationTemplateInfo relationTemplate = this.stubService.getEMM().getRelationTemplateById(rt.getTemplateID());
					if (relationTemplate != null)
					{
						relationTemplate = this.stubService.getEMM().getRelationTemplateByName(mainObjectGuid, relationTemplate.getName());
						if (relationTemplate != null)
						{
							ViewObject viewObject = this.stubService.getRelationByEND1(mainObjectGuid, relationTemplate.getName());
							if (viewObject != null)
							{
								//todo
								List<StructureObject> assoDataList = this.stubService.getRelationService().listObjectOfRelation(viewObject.getObjectGuid(), relationTemplate.getGuid(),
										null, checkAcl, null, null);
								if (!SetUtils.isNullList(assoDataList))
								{
									for (StructureObject assoObj : assoDataList)
									{
										if (assoObj.getEnd2ObjectGuid().getGuid() != null && !distinctSet.contains(assoObj.getEnd2ObjectGuid().getGuid()))
										{
											distinctSet.add(assoObj.getEnd2ObjectGuid().getGuid());
											assoEnd2List.add((FoundationObject) assoObj.get(ViewObject.PREFIX_END2));
										}
									}
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

	}


}
