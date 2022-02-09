/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: StructureClassNameDecorator
 * Wanglei 2010-10-13
 */
package dyna.app.service.helper.decorate;

import dyna.common.bean.data.DynaObject;
import dyna.common.bean.data.foundation.ViewObject;
import dyna.common.exception.DecorateException;
import dyna.net.service.brs.EMM;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 *
 */
@Component
public class ViewClassNameDecorator extends ClassNameDecorator
{

	public <T extends DynaObject> void decorate(T object, EMM emm) throws DecorateException
	{
		super.decorate(object, emm, null);
		this.decorate(ViewObject.END1CLASSGUID, ViewObject.END1CLASSNAME, object, emm);
	}

}
