package dyna.net.service;

import dyna.common.conf.ServiceDefinition;

/**
 * @author Lizw
 * @date 2022/2/16
 **/
public interface ApplicationService
{

	/**
	 * 初始化服务
	 */
	void init(ServiceDefinition serviceDefinition);
}
