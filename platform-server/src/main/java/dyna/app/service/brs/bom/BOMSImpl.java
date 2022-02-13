/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: BOMImpl
 * caogc 2010-10-12
 */
package dyna.app.service.brs.bom;

import dyna.app.core.track.annotation.Tracked;
import dyna.app.service.BusinessRuleService;
import dyna.app.service.brs.bom.tracked.TRSaveStructureImpl;
import dyna.app.service.helper.TrackedDesc;
import dyna.common.SearchCondition;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.foundation.BOMView;
import dyna.common.bean.data.structure.BOMStructure;
import dyna.common.dto.DataRule;
import dyna.common.dto.ECEffectedBOMRelation;
import dyna.common.dto.template.bom.BOMCompare;
import dyna.common.exception.AuthorizeException;
import dyna.common.exception.ServiceRequestException;
import dyna.net.service.brs.*;
import dyna.net.service.das.JSS;
import dyna.net.service.data.AclService;
import dyna.net.service.data.InstanceService;
import dyna.net.service.data.RelationService;
import dyna.net.service.data.SystemDataService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Bill of material implementation
 *
 * @author Lizw
 */
@Service public class BOMSImpl extends BusinessRuleService implements BOMS
{
	@DubboReference private AclService        aclService;
	@DubboReference private InstanceService   instanceService;
	@DubboReference private RelationService   relationService;
	@DubboReference private SystemDataService systemDataService;

	@Autowired private BOMStub      bomStub;
	@Autowired private BOMViewStub                 bomViewStub;
	@Autowired private BOMEditStub                 bomEditStub;
	@Autowired private BOMCompareStub              bomCompareStub;
	@Autowired private BOMViewCheckInStub          bomViewCheckInStub;
	@Autowired private BOMViewCheckOutStub         bomViewCheckOutStub;
	@Autowired private BOMViewCancelCheckOutStub   bomViewCancelCheckOutStub;
	@Autowired private BOMViewTransferCheckOutStub bomViewTransferCheckOutStub;
	@Autowired private DrawTransferBOMStub         drawTransferBOMStub;
	@Autowired private AAS                         aas;
	@Autowired private ACL                         acl;
	@Autowired private Async                       async;
	@Autowired private BOAS                        boas;
	@Autowired private BRM                         brm;
	@Autowired private DCR                         dcr;
	@Autowired private JSS                         jss;
	@Autowired private WFI                         wfi;

	protected AclService getAclService()
	{
		return this.aclService;
	}

	protected InstanceService getInstanceService()
	{
		return this.instanceService;
	}

	protected RelationService getRelationService()
	{
		return this.relationService;
	}

	protected SystemDataService getSystemDataService()
	{
		return this.systemDataService;
	}

	protected AAS getAAS()
	{
		return this.aas;
	}

	protected ACL getACL()
	{
		return this.acl;
	}

	protected Async getAsync()
	{
		return this.async;
	}

	protected BOAS getBOAS()
	{
		return this.boas;
	}

	protected BRM getBRM()
	{
		return this.brm;
	}

	protected JSS getJss()
	{
		return this.jss;
	}

	protected WFI getWFI()
	{
		return this.wfi;
	}

	protected DCR getDCR()
	{
		return this.dcr;
	}

	public BOMCompareStub getBomCompareStub()
	{
		return this.bomCompareStub;
	}

	public BOMEditStub getBomEditStub()
	{
		return this.bomEditStub;
	}

	public BOMStub getBomStub()
	{
		return this.bomStub;
	}

	public BOMViewStub getBomViewStub()
	{
		return this.bomViewStub;
	}

	public DrawTransferBOMStub getDrawTransferBOMStub()
	{
		return this.drawTransferBOMStub;
	}

	protected synchronized EMM getEMM() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(EMM.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	protected synchronized EOSS getEOSS() throws ServiceRequestException
	{
		try
		{
			return this.getRefService(EOSS.class);
		}
		catch (Exception e)
		{
			throw new ServiceRequestException(null, e.getMessage(), e.fillInStackTrace());
		}

	}

	/**
	 * @return the bomViewCheckInStub
	 */
	public BOMViewCheckInStub getBOMViewCheckInStub()
	{
		return this.bomViewCheckInStub;
	}

	/**
	 * @return the bomViewCancelCheckOutStub
	 */
	public BOMViewCancelCheckOutStub getBomViewCancelCheckOutStub()
	{
		return this.bomViewCancelCheckOutStub;
	}

	/**
	 * @return the bomViewTransferCheckOutStub
	 */
	public BOMViewTransferCheckOutStub getBomViewTransferCheckOutStub()
	{
		return this.bomViewTransferCheckOutStub;
	}

	/**
	 * @return the bomViewCheckOutStub
	 */
	public BOMViewCheckOutStub getBOMViewCheckOutStub()
	{
		return this.bomViewCheckOutStub;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * dyna.app.service.BusinessRuleService#authorize(dyna.net.security.signature
	 * .Signature)
	 */
	@Override public void authorize(Method method, Object... args) throws AuthorizeException
	{
		super.authorize(method, args);
	}

	@Override public boolean canEditBom(FoundationObject end1Object, String name) throws ServiceRequestException
	{
		return this.getBomEditStub().canEditBom(end1Object, name);
	}

	@Override public List<BOMCompare> compareBOM(ObjectGuid leftBOMViewObjectGuid, DataRule leftDataRule, ObjectGuid rightBOMViewObjectGuid, DataRule rightDataRule)
			throws ServiceRequestException
	{
		return this.getBomCompareStub().compareBOM(leftBOMViewObjectGuid, leftDataRule, rightBOMViewObjectGuid, rightDataRule);
	}

	@Override public void deleteBOMView(BOMView bomView) throws ServiceRequestException
	{
		this.getBomViewStub().deleteBOMView(bomView, true);
	}

	@Override public List<BOMStructure> editBOM(BOMView itemView, List<ECEffectedBOMRelation> ecEffectedBOMRelationList, String procRtGuid, String tempalteName)
			throws ServiceRequestException
	{
		return this.getBomEditStub().editBOM(itemView, ecEffectedBOMRelationList, true, procRtGuid, tempalteName, true);
	}

	@Override public BOMStructure getBOM(ObjectGuid objectGuid, SearchCondition searchCondition, DataRule dataRule) throws ServiceRequestException
	{
		return this.getBomStub().getBOM(objectGuid, searchCondition, true, dataRule);
	}

	@Override public BOMView getBOMView(ObjectGuid objectGuid) throws ServiceRequestException
	{
		return this.getBomViewStub().getBOMView(objectGuid, true);
	}

	@Override public BOMView getBOMViewByEND1(ObjectGuid end1ObjectGuid, String viewName) throws ServiceRequestException
	{
		return this.getBomViewStub().getBOMViewByEND1(end1ObjectGuid, viewName, true);
	}

	// @Tracked(description = TrackedDesc.LINK_BOM, renderer =
	// TRViewLinkImpl.class)
	@Override public BOMStructure linkBOM(ObjectGuid bomView, ObjectGuid end2FoundationObject, BOMStructure bomStructure) throws ServiceRequestException
	{
		return this.getBomEditStub().linkBOM(bomView, end2FoundationObject, bomStructure, true, null, true, true);
	}

	// @Tracked(description = TrackedDesc.LINK_BOM, renderer =
	// TREnd1LinkImpl.class)
	@Override public BOMStructure linkBOM(ObjectGuid end1Object, ObjectGuid end2Object, BOMStructure bomStructure, String viewName, boolean isPrecise)
			throws ServiceRequestException
	{
		return this.getBomEditStub().linkBOM(end1Object, end2Object, bomStructure, viewName, isPrecise, true, null, true);
	}

	public BOMStructure linkBOM(ObjectGuid end1Object, ObjectGuid end2Object, BOMStructure bomStructure, String viewName, boolean isPrecise, boolean isCheckAcl)
			throws ServiceRequestException
	{
		return this.getBomEditStub().linkBOM(end1Object, end2Object, bomStructure, viewName, isPrecise, isCheckAcl, null, true);
	}

	@Override public List<BOMStructure> listBOM(ObjectGuid viewObjectGuid, SearchCondition searchCondition, SearchCondition end2SearchCondition, DataRule dataRule)
			throws ServiceRequestException
	{
		return this.getBomStub().listBOM(viewObjectGuid, searchCondition, end2SearchCondition, dataRule);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * dyna.net.service.brs.BOAS#listObjectByBOM(dyna.common.bean.data.ObjectGuid
	 * , dyna.common.bean.data.system.BOMRule, java.lang.String,
	 * dyna.common.bean.data.system.Folder)
	 */
	@Override public List<BOMStructure> listBOM(ObjectGuid end1ObjectGuid, String templateName, SearchCondition searchCondition, SearchCondition end2SearchCondition,
			DataRule dataRule) throws ServiceRequestException
	{
		return this.getBomStub().listBOM(end1ObjectGuid, templateName, searchCondition, end2SearchCondition, dataRule);
	}

	@Override public List<BOMView> listBOMView(ObjectGuid end1ObjectGuid) throws ServiceRequestException
	{
		return this.getBomViewStub().listBOMView(end1ObjectGuid);
	}

	@Override public List<FoundationObject> listWhereUsed(ObjectGuid objectGuid, String viewName, SearchCondition end1SearchCondition, SearchCondition structureSearchCondition,
			DataRule dataRule) throws ServiceRequestException
	{
		return this.getBomStub().listWhereUsed(objectGuid, viewName, end1SearchCondition, structureSearchCondition, dataRule, false);
	}

	@Override public List<FoundationObject> listWhereUsed(ObjectGuid objectGuid, String viewName, SearchCondition end1SearchCondition, SearchCondition structureSearchCondition,
			DataRule dataRule, boolean viewHistory) throws ServiceRequestException
	{
		return this.getBomStub().listWhereUsed(objectGuid, viewName, end1SearchCondition, structureSearchCondition, dataRule, viewHistory);
	}

	@Tracked(description = TrackedDesc.SAVE_BOM, renderer = TRSaveStructureImpl.class) @Override public BOMStructure saveBOM(BOMStructure bomStructure)
			throws ServiceRequestException
	{
		return this.getBomStub().saveBOM(bomStructure);
	}

	@Override public BOMView saveBOMView(BOMView bomView, String procRtGuid) throws ServiceRequestException
	{
		return this.getBomViewStub().saveBOMView(bomView, true, procRtGuid);
	}

	@Override public BOMView saveBOMView(BOMView bomView) throws ServiceRequestException
	{
		return this.getBomViewStub().saveBOMView(bomView, true, null);
	}

	@Override public BOMView createBOMViewByBOMTemplate(String bomTemplateGuid, ObjectGuid end1ObjectGuid, boolean isPrecise) throws ServiceRequestException
	{
		return this.getBomViewStub().createBOMViewByBOMTemplate(bomTemplateGuid, end1ObjectGuid, isPrecise, true);
	}

	@Override public void unlinkBOM(BOMStructure bomStructure) throws ServiceRequestException
	{
		this.getBomEditStub().unlinkBOM(bomStructure, true);
	}

	@Override public BOMView updateBOMViewPrecise(BOMView bomView) throws ServiceRequestException
	{
		return this.getBomViewStub().updateBOMViewPrecise(bomView);
	}

	@Override public BOMView checkIn(BOMView bomView) throws ServiceRequestException
	{
		return this.getBOMViewCheckInStub().checkIn(bomView, true);
	}

	@Override public BOMView checkOut(BOMView bomView) throws ServiceRequestException
	{
		String checkOutUserGuid = this.getOperatorGuid();
		return this.getBOMViewCheckOutStub().checkOut(bomView, checkOutUserGuid, true);
	}

	@Override public BOMView cancelCheckOut(BOMView bomView) throws ServiceRequestException
	{
		return this.getBomViewCancelCheckOutStub().cancelCheckOut(bomView);
	}

	@Override public BOMView transferCheckout(BOMView bomView, String toUserGuid) throws ServiceRequestException
	{
		return this.getBomViewTransferCheckOutStub().transferCheckout(bomView, toUserGuid, true);
	}

	@Override public List<BOMStructure> listBOMForList(ObjectGuid viewObjectGuid, SearchCondition searchCondition, SearchCondition end2SearchCondition, DataRule dataRule)
			throws ServiceRequestException
	{
		return this.getBomStub().listBOMForList(viewObjectGuid, searchCondition, end2SearchCondition, dataRule, true);
	}

	@Override public Map<String, List<BOMStructure>> listBOMForTree(ObjectGuid viewObjectGuid, int level, SearchCondition searchCondition, SearchCondition end2SearchCondition,
			DataRule dataRule) throws ServiceRequestException
	{
		return this.getBomStub().listBOMForTreeCheckACL(viewObjectGuid, level, searchCondition, end2SearchCondition, dataRule, true);
	}

	@Override public void unLinkAllBOMStructure(ObjectGuid end1ObjectGuid, String viewName) throws ServiceRequestException
	{
		this.getBomStub().unLinkAllBOMStructure(end1ObjectGuid, viewName);
	}

	@Override public List<FoundationObject> listFoundationObjectOfBOMView(ObjectGuid bomViewObjectGuid, SearchCondition searchCondition, DataRule dataRule)
			throws ServiceRequestException
	{
		return this.getBomStub().listFoundationObjectOfBOMView(bomViewObjectGuid, searchCondition, dataRule);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * dyna.net.service.brs.BOMS#compareBOMByObject(dyna.common.bean.data.ObjectGuid
	 * , dyna.common.bean.data.system.BOMRule, dyna.common.bean.data.ObjectGuid,
	 * dyna.common.bean.data.system.BOMRule)
	 */
	@Override public List<BOMCompare> compareBOMByObject(ObjectGuid leftBOMViewObjectGuid, DataRule leftDataRule, ObjectGuid rightBOMViewObjectGuid, DataRule rightDataRule)
			throws ServiceRequestException
	{
		return this.getBomCompareStub().compareBOMByObject(leftBOMViewObjectGuid, leftDataRule, rightBOMViewObjectGuid, rightDataRule);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.net.service.brs.BOMS#newStructureObject(java.lang.String,
	 * java.lang.String)
	 */
	@Override public BOMStructure newBOMStructure(String classGuid, String className) throws ServiceRequestException
	{
		return this.getBomStub().newBOMStructure(classGuid, className);
	}

	@Override public boolean cadToBom(FoundationObject cad, String bomTemplateGuid, boolean isRoot, FoundationObject end1, boolean isYS) throws ServiceRequestException
	{
		return this.getBomEditStub().cadToBom(cad, bomTemplateGuid, isRoot, end1, isYS);
	}

	@Override public List<BOMStructure> editBOM(BOMView itemView, List<ECEffectedBOMRelation> ecEffectedBOMRelationList, String procRtGuid, String templateName,
			boolean isCheckField) throws ServiceRequestException
	{
		return this.getBomEditStub().editBOM(itemView, ecEffectedBOMRelationList, true, procRtGuid, templateName, isCheckField);
	}

	@Override public BOMStructure saveBOM(BOMStructure bomStructure, boolean isCheckField) throws ServiceRequestException
	{
		return this.getBomStub().saveBOM(bomStructure, true, isCheckField, true);
	}

	@Override public BOMStructure saveBOM(BOMStructure bomStructure, boolean isCheckField, boolean isCheckConnect) throws ServiceRequestException
	{
		return this.getBomStub().saveBOM(bomStructure, true, isCheckField, isCheckConnect);
	}

	@Override public BOMView updateBomViewOwner(ObjectGuid objectGuid, String ownerUserGuid, String ownerGroupGuid, Date updateTime) throws ServiceRequestException
	{
		return this.getBomViewStub().updateBomViewOwner(objectGuid, ownerUserGuid, ownerGroupGuid, updateTime, true);
	}

	@Override public List<FoundationObject> batchListEnd1OfBOMView(List<BOMView> bomViewList) throws ServiceRequestException
	{
		return this.getBomViewStub().batchListEnd1OfBOMView(bomViewList);
	}

	@Override public void drawTransferToBOM(ObjectGuid end1ObjectGuid, String bomTemplateName, String procRtGuid) throws ServiceRequestException
	{
		this.getDrawTransferBOMStub().transferBOM(end1ObjectGuid, bomTemplateName, procRtGuid);
	}

}
