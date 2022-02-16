/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: SystemIDChecker
 * Wanglei 2011-4-15
 */
package dyna.app.core.lic.system;

import java.util.List;

import dyna.common.log.DynaLogger;
import dyna.common.util.EnvUtils;

/**
 * @author Wanglei
 *
 */
public interface SystemIDChecker
{
	/**
	 * 通过licence获取系统标识符
	 * 
	 * @param licenseRawData
	 * @return
	 */
	public String getSystemIdentification(List<String> licenseRawData);
	
	public boolean isVM();
}
