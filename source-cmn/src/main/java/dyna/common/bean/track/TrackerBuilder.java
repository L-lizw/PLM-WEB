/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: TrackerBuilder
 * Wanglei 2011-11-9
 */
package dyna.common.bean.track;


import dyna.common.bean.signature.Signature;
import dyna.common.systemenum.LanguageEnum;

import java.lang.reflect.Method;

/**
 * 操作日志构建器
 * 
 * @author Wanglei
 * 
 */
public interface TrackerBuilder
{
	/**
	 * 组建Tracker
	 * 
	 * @return
	 */
	public Tracker make(Signature signature, Method method, Object result, Object[] parameters, LanguageEnum languageEnum);

	public void setTrackerRendererClass(Class<? extends TrackerRenderer> rendererClass, String desc);

	public void setPersistenceClass(Class<? extends TrackerPersistence> tpClass);
}
