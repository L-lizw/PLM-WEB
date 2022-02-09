/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: DecoratorFactory
 * Wanglei 2010-9-2
 */
package dyna.app.service.helper.decorate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ShortObject;
import dyna.common.bean.data.StructureObject;
import dyna.common.bean.data.structure.BOMStructure;
import dyna.common.dto.Folder;
import dyna.common.dto.MailAttachment;
import dyna.common.exception.DecorateException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.SystemClassFieldEnum;
import dyna.common.util.StringUtils;
import dyna.net.service.brs.EMM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 着色器工厂
 *
 * @author Lizw
 */
@Component public class DecoratorFactory
{
	@Qualifier("classNameDecorator") public ClassNameDecorator          cnd;
	@Autowired public ViewClassNameDecorator      vcnd;
	@Autowired public StructureCLassNameDecorator scnd;
	@Autowired public MailAttClassNameDecorator   mcnd;
	@Qualifier("FullNameDecorator")
	public            FullNameDecorator           fnd ;
	@Autowired
	public            StructureFullNameDecorator  sfnd;
	@Autowired
	public            LCPhaseDecorator            lcpd;
	@Qualifier("BizObjectDecorator")
	public            BizObjectDecorator          bod;
	@Autowired
	public StructureBODecorator sbod;
	@Autowired
	public CodeTitleDecorator ctd;
	@Autowired
	public ObjectFieldDecorator ofd;

	public Set<String> shortObjectCodeFieldSet = new HashSet<String>();

	{
		shortObjectCodeFieldSet.add(SystemClassFieldEnum.CLASSIFICATION.getName());
	}

	public void decorateCodeRule(Set<String> objectFieldName, Set<String> codeFieldName, ShortObject object, EMM emm) throws DecorateException
	{
		cnd.decorateWithField(objectFieldName, object, emm);
		ctd.decorateWithField(codeFieldName, object, emm);
	}

	public void decorateClassificaitonObject(Set<String> fieldNames, FoundationObject object, EMM emm, String bmGuid, Folder folder) throws DecorateException
	{
		cnd.decorateWithField(fieldNames, object, emm);

		if (!StringUtils.isNullString(bmGuid))
		{
			bod.decorateWithField(fieldNames, object, bmGuid, emm);
		}
	}

	public void decorateFoundationObject(Set<String> fieldNames, List<FoundationObject> objectList, EMM emm, String sessionId) throws DecorateException
	{
		ofd.decorateWithField(fieldNames, objectList, emm, sessionId, false);
	}

	public void decorateStructureObject(Set<String> fieldNames, List<StructureObject> objectList, EMM emm, String sessionId) throws DecorateException
	{
		ofd.decorateWithField(fieldNames, objectList, emm, sessionId, false);
	}

	public void decorateBOMStructure(Set<String> fieldNames, List<BOMStructure> objectList, EMM emm, String sessionId) throws DecorateException
	{
		ofd.decorateWithField(fieldNames, objectList, emm, sessionId, false);
	}

	public void decorateFoundationObject(Set<String> fieldNames, FoundationObject object, EMM emm, String bmGuid, Folder folder) throws DecorateException
	{
		// 主对象的 CLASS$ICON32,CLASS$ICON,CLASSNAME设值
		cnd.decorate(object, emm, folder);
		object.resetObjectGuid();

		// 对象中object类型的字段的 NAME,CLASS$ICON32,CLASS$ICON,CLASSNAME 设值
		cnd.decorateWithField(fieldNames, object, emm);

		if (!StringUtils.isNullString(bmGuid))
		{
			// 主对象BOTITLE,BOGUID 设值
			bod.decorate(object, bmGuid, emm);
			// 对象中object类型的字段的BOTITLE,BOGUID 设值
			bod.decorateWithField(fieldNames, object, bmGuid, emm);
		}
		// 主对象的 name设值
		fnd.decorate(object, emm);
		lcpd.decorate(object, emm);
		// erd.decorate(object, emm);
	}

	public void decorateFoundationObjectCode(Set<String> fieldNames, FoundationObject object, EMM emm, String bmGuid) throws DecorateException
	{
		ctd.decorateWithField(fieldNames, object, emm);
	}

	public void decorateMailAttachment(MailAttachment object, EMM emm) throws DecorateException
	{
		mcnd.decorate(object, emm);
	}

	public void decorateShortObject(ShortObject object, EMM emm, String bmGuid, Folder folder) throws DecorateException
	{
		cnd.decorate(object, emm, folder);
		bod.decorate(object, bmGuid, emm);

		ctd.decorateWithField(shortObjectCodeFieldSet, object, emm);

		fnd.decorate(object, emm);
		lcpd.decorate(object, emm);
	}

	public void decorateStructureObject(StructureObject object, Set<String> objectFieldNames, Set<String> codeFieldNames, EMM emm, String bmGuid)
			throws DecorateException, ServiceRequestException
	{
		scnd.decorate(object, emm);

		cnd.decorate(object, emm, null);
		object.resetObjectGuid();

		if (objectFieldNames != null)
		{
			// Object's className
			cnd.decorateWithField(objectFieldNames, object, emm);
		}

		if (object instanceof BOMStructure)
		{
			if (codeFieldNames == null)
			{
				codeFieldNames = new HashSet<String>();
			}
			if (!codeFieldNames.contains(BOMStructure.UOM))
			{
				codeFieldNames.add(BOMStructure.UOM);
			}

		}
		if (codeFieldNames != null)
		{
			// Code's Title
			ctd.decorateWithField(codeFieldNames, object, emm);
		}
		sfnd.decorate(object, emm);

		sbod.decorate(object, bmGuid, emm);

		lcpd.decorate(object, emm);
	}

	public void decorateViewObject(ShortObject object, EMM emm, String bmGuid) throws DecorateException
	{
		vcnd.decorate(object, emm);
		fnd.decorate(object, emm);
		bod.decorate(object, bmGuid, emm);
	}
}
