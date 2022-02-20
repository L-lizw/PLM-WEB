package dyna.app.service.brs.mms;

import dyna.app.service.AbstractServiceStub;
import dyna.common.bean.model.code.CodeItem;
import dyna.common.bean.model.code.CodeObject;
import dyna.common.dto.model.cls.ClassField;
import dyna.common.dto.model.code.CodeItemInfo;
import dyna.common.dto.model.code.CodeObjectInfo;
import dyna.common.exception.ServiceRequestException;
import dyna.common.util.SetUtils;
import dyna.dbcommon.filter.FieldValueEqualsFilter;
import dyna.net.security.signature.UserSignature;
import dyna.net.service.data.SystemDataService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CodeManageModifyStub extends AbstractServiceStub<MMSImpl>
{

	protected List<CodeObjectInfo> copy4CreateCodeObject(List<CodeObjectInfo> sourceCodeObjectInfoList, boolean isCut) throws ServiceRequestException
	{
		List<CodeObjectInfo> resultlist = new ArrayList<CodeObjectInfo>();
		if (!SetUtils.isNullList(sourceCodeObjectInfoList))
		{
			for (CodeObjectInfo codeObjectInfo : sourceCodeObjectInfoList)
			{
				resultlist.add(this.createCodeObject(codeObjectInfo, isCut));

			}
		}
		return resultlist;
	}

	protected CodeObjectInfo createCodeObject(CodeObjectInfo sourceCodeObjectInfo, boolean needDelSource) throws ServiceRequestException
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		String currentUserguid = this.stubService.getUserSignature().getUserGuid();

//		this.stubService.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());
		try
		{
			CodeObjectInfo newCodeObejctInfo = sourceCodeObjectInfo.clone();
			newCodeObejctInfo.setGuid(null);
			newCodeObejctInfo.setCreateUserGuid(currentUserguid);
			newCodeObejctInfo.setUpdateUserGuid(currentUserguid);
			sds.save(newCodeObejctInfo);

			List<CodeItemInfo> firstLevelCodeItemList = this.stubService.getEmm().listSubCodeItemForMaster(sourceCodeObjectInfo.getGuid(), null);
			if (!SetUtils.isNullList(firstLevelCodeItemList))
			{
				for (CodeItemInfo codeItemInfo : firstLevelCodeItemList)
				{
					this.createCodeItem(codeItemInfo, newCodeObejctInfo.getGuid(), null, needDelSource);
				}
			}

			if (needDelSource)
			{
				sds.delete(sourceCodeObjectInfo);
			}

//			this.stubService.getTransactionManager().commitTransaction();

			return newCodeObejctInfo;
		}
		catch (Exception e)
		{
//			this.stubService.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}
	}

	protected void copy4createCodeItem(List<CodeItemInfo> sourcCodeItemInfoList, String codeObjectguid, String parentCodeItemguid) throws ServiceRequestException
	{
		if (!SetUtils.isNullList(sourcCodeItemInfoList))
		{
			for (CodeItemInfo codeItemInfo : sourcCodeItemInfoList)
			{
				this.createCodeItem(codeItemInfo, codeObjectguid, parentCodeItemguid, false);
			}
		}
	}

	protected void createCodeItem(CodeItemInfo sourcCodeItemInfo, String codeObjectguid, String parentCodeItemguid, boolean needDelSource) throws ServiceRequestException
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		String currentUserguid = this.stubService.getUserSignature().getUserGuid();

//		this.stubService.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());
		try
		{
			CodeItemInfo newCodeItemInfo = sourcCodeItemInfo.clone();
			newCodeItemInfo.setGuid(null);
			newCodeItemInfo.setCodeGuid(codeObjectguid);
			newCodeItemInfo.setParentGuid(parentCodeItemguid);
			newCodeItemInfo.setCreateUserGuid(currentUserguid);
			newCodeItemInfo.setUpdateUserGuid(currentUserguid);
			sds.save(newCodeItemInfo);

			List<CodeItemInfo> subCodeItemInfoList = this.stubService.getEmm().listSubCodeItemForDetail(sourcCodeItemInfo.getGuid());
			if (!SetUtils.isNullList(subCodeItemInfoList))
			{
				for (CodeItemInfo subCodeItemInfo : subCodeItemInfoList)
				{
					this.createCodeItem(subCodeItemInfo, codeObjectguid, newCodeItemInfo.getGuid(), needDelSource);
				}
			}

			if (needDelSource)
			{
				sds.delete(sourcCodeItemInfo);
			}

//			this.stubService.getTransactionManager().commitTransaction();

		}
		catch (Exception e)
		{
//			this.stubService.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}

	}

	protected void editCodeObject(CodeObject sourceCodeObject) throws ServiceRequestException
	{
//		this.stubService.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());
		SystemDataService sds = this.stubService.getSystemDataService();
		String currentUserGuid = ((UserSignature) this.stubService.getSignature()).getUserGuid();

		try
		{
			CodeObjectInfo codeObjectInfo = sourceCodeObject.getInfo();
			Map<String, String> nameGuidMap = new HashMap<String, String>();
			if (codeObjectInfo.getGuid() == null)
			{
				codeObjectInfo.setCreateUserGuid(currentUserGuid);
			}
			else
			{
				List<CodeItemInfo> existsList = sds.listFromCache(CodeItemInfo.class, new FieldValueEqualsFilter<CodeItemInfo>(CodeItemInfo.MASTERGUID, codeObjectInfo.getGuid()));
				if (!SetUtils.isNullList(existsList))
				{
					for (CodeItemInfo codeItemInfo : existsList)
					{
						nameGuidMap.put(codeItemInfo.getName(), codeItemInfo.getGuid());
					}
				}
			}
			codeObjectInfo.setUpdateUserGuid(currentUserGuid);
			sds.save(codeObjectInfo);

			List<CodeItem> codeItemList = sourceCodeObject.getCodeDetailList();
			if (!SetUtils.isNullList(codeItemList))
			{
				int sequence = 0;
				for (CodeItem codeItem : codeItemList)
				{
					codeItem.setGuid(nameGuidMap.get(codeItem.getName()));
					nameGuidMap.remove(codeItem.getName());
					this.editCodeItem(codeItem, codeObjectInfo.getGuid(), null, sequence++, nameGuidMap);
				}
			}

			if (!SetUtils.isNullMap(nameGuidMap))
			{
				for (Map.Entry<String, String> entry : nameGuidMap.entrySet())
				{
					sds.delete(CodeItemInfo.class, entry.getValue());
				}
			}

//			this.stubService.getTransactionManager().commitTransaction();

		}
		catch (Exception e)
		{
//			this.stubService.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}

	}

	private void editCodeItem(CodeItem codeItem, String masterGuid, String parentItemGuid, int sequence, Map<String, String> nameGuidMap) throws ServiceRequestException
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		String currentUserGuid = ((UserSignature) this.stubService.getSignature()).getUserGuid();

//		this.stubService.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());

		try
		{
			CodeItemInfo codeItemInfo = codeItem.getInfo().clone();
			codeItemInfo.setSequence(sequence);
			codeItemInfo.setMasterGuid(masterGuid);
			codeItemInfo.setParentGuid(parentItemGuid);
			codeItemInfo.setCreateUserGuid(currentUserGuid);
			codeItemInfo.setUpdateUserGuid(currentUserGuid);
			sds.save(codeItemInfo);

			List<CodeItem> codeItemList = codeItem.getCodeDetailList();
			if (!SetUtils.isNullList(codeItemList))
			{
				int subSequence = 0;
				for (CodeItem subItem : codeItemList)
				{
					subItem.setGuid(nameGuidMap.get(subItem.getName()));
					nameGuidMap.remove(codeItem.getName());
					this.editCodeItem(subItem, masterGuid, codeItemInfo.getGuid(), subSequence++, nameGuidMap);
				}
			}

//			this.stubService.getTransactionManager().commitTransaction();

		}
		catch (Exception e)
		{
//			this.stubService.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}

	}

	protected void saveCodeItem(CodeItemInfo codeItemInfo) throws ServiceRequestException
	{
		SystemDataService systemDataService = this.stubService.getSystemDataService();
		String ownerUserGuid = ((UserSignature) stubService.getSignature()).getUserGuid();
		codeItemInfo.setUpdateUserGuid(ownerUserGuid);
		systemDataService.save(codeItemInfo);
	}

	protected void deleteCodeObject(String codeguid) throws ServiceRequestException
	{
		try
		{
//			this.stubService.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());

			SystemDataService sds = this.stubService.getSystemDataService();

			sds.deleteFromCache(CodeItemInfo.class, new FieldValueEqualsFilter<CodeItemInfo>(CodeItemInfo.MASTERGUID, codeguid));

			sds.delete(CodeObjectInfo.class, codeguid);
//			this.stubService.getTransactionManager().commitTransaction();
		}
		catch (Exception e)
		{
//			this.stubService.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}
	}

	protected void deleteCodeItem(String codeItemGuid) throws ServiceRequestException
	{
		try
		{
//			this.stubService.getTransactionManager().startTransaction(this.stubService.getFixedTransactionId());
			SystemDataService sds = this.stubService.getSystemDataService();

			this.deleteChildCodeItem(codeItemGuid);

			sds.delete(CodeItemInfo.class, codeItemGuid);
//			this.stubService.getTransactionManager().commitTransaction();
		}
		catch (Exception e)
		{
//			this.stubService.getTransactionManager().rollbackTransaction();
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}
	}

	private void deleteChildCodeItem(String parentItemGuid) throws ServiceRequestException
	{
		try
		{
			SystemDataService sds = this.stubService.getSystemDataService();

			List<CodeItemInfo> childList = sds.listFromCache(CodeItemInfo.class, new FieldValueEqualsFilter<CodeItemInfo>(CodeItemInfo.PARENTGUID, parentItemGuid));
			if (!SetUtils.isNullList(childList))
			{
				for (CodeItemInfo itemInfo : childList)
				{
					this.deleteChildCodeItem(itemInfo.getGuid());
					sds.delete(itemInfo);
				}
			}
		}
		catch (Exception e)
		{
			if (e instanceof ServiceRequestException)
			{
				throw (ServiceRequestException) e;
			}
			else
			{
				throw ServiceRequestException.createByException("ID_APP_SERVER_EXCEPTION", e);
			}
		}
	}

	protected void updateClassificationField(String codeItemGuid, List<ClassField> fieldList) throws ServiceRequestException
	{

	}

}
