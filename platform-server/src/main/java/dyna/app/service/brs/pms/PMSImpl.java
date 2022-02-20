/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ECSImpl
 * caogc 2010-11-02
 */
package dyna.app.service.brs.pms;

import dyna.app.service.BusinessRuleService;
import dyna.common.SearchCondition;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.dto.ItemProduct;
import dyna.common.dto.template.relation.RelationTemplateInfo;
import dyna.common.exception.AuthorizeException;
import dyna.common.exception.ServiceRequestException;
import dyna.net.service.brs.*;
import dyna.net.service.data.SystemDataService;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Engineering Change Service Implement工程变更服务的实现类
 *
 * @author Lizw
 */
@Getter(AccessLevel.PROTECTED)
@Service public class PMSImpl extends BusinessRuleService implements PMS
{
	private static boolean initialized = false;

	@DubboReference private SystemDataService systemDataService;

	@Autowired
	private AAS aas;
	@Autowired
	private BOAS boas;
	@Autowired
	private BOMS boms;
	@Autowired
	private EMM emm;
	@Autowired
	private POS pos;

	@Autowired private Async async;

	@Autowired private ItemProductStub itemProductStub ;

	protected SystemDataService getSystemDataService()
	{
		return this.systemDataService;
	}

	protected Async getAsync()
	{
		return this.async;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see dyna.app.service.BusinessRuleService#authorize(dyna.net.security.signature.Signature)
	 */
	@Override public void authorize(Method method, Object... args) throws AuthorizeException
	{
		super.authorize(method, args);
	}

	@Override public void deleteItemProduct(List<String> itemProductGuidList) throws ServiceRequestException
	{
		this.getItemProductStub().deleteItemProduct(itemProductGuidList);
	}

	/**
	 * @return the ecItemProductStub
	 */
	public ItemProductStub getItemProductStub()
	{
		return this.itemProductStub;
	}

	@Override public List<ItemProduct> listItemProductByItem(ObjectGuid itemObjectGuid) throws ServiceRequestException
	{
		return this.getItemProductStub().listItemProductByItem(itemObjectGuid);
	}

	@Override public void deleteItemProductByItem(ObjectGuid itemObjectGuid) throws ServiceRequestException
	{
		this.getItemProductStub().deleteItemProductByItem(itemObjectGuid);
	}

	@Override public void addToProduct(ObjectGuid itemObjectGuid, List<ObjectGuid> productObjectGuidList) throws ServiceRequestException
	{
		this.getItemProductStub().addToProduct(itemObjectGuid, productObjectGuidList);
	}

	@Override public List<FoundationObject> listProductSummaryObject(ObjectGuid productObjectGuid, SearchCondition searchCondition, String viewList) throws ServiceRequestException
	{
		return this.getItemProductStub().listProductSummaryObject(productObjectGuid, searchCondition, viewList, true);
	}

	@Override public List<RelationTemplateInfo> listProductSummaryRelationTemplate(ObjectGuid productObjectGuid) throws ServiceRequestException
	{
		return this.getItemProductStub().listProductSummaryRelationTemplate(productObjectGuid);
	}
}
