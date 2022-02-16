/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: SystemIDCheckerLinuxImpl
 * Wanglei 2011-4-15
 */
package dyna.app.core.lic.system;

import java.util.List;

import dyna.app.core.lic.mac.MacAddressMatcher;
import dyna.app.core.lic.mac.MacAddressMatcherLinuxImpl;
import dyna.common.util.EnvUtils;
import org.springframework.stereotype.Component;

/**
 * @author Lizw
 *
 */
@Component
public class SystemIDCheckerLinuxImpl extends AbstractSystemIDChecker
{
	private MacAddressMatcher	macAddressMatcher	;

	protected SystemIDCheckerLinuxImpl()
	{
		super(EnvUtils.getOSName());
		macAddressMatcher = new MacAddressMatcherLinuxImpl();
	}

	/* (non-Javadoc)
	 * @see dyna.app.core.lic.system.AbstractSystemIDCheckeer#lookupSystemIdentification(java.util.List)
	 */
	@Override
	protected String lookupSystemIdentification(List<String> licenseRawData)
	{
		return this.macAddressMatcher.getGrantMacAddress(licenseRawData);
	}

}
