/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ServerContext
 * Wanglei 2010-3-30
 */
package dyna.app.server.context;

import dyna.app.server.core.i18n.NLSManager;
import dyna.app.server.core.lic.LicenseDaemon;
import dyna.app.server.core.track.TrackerManager;
import dyna.app.conf.yml.ConfigurableServerImpl;
import dyna.common.context.SvContext;
import dyna.net.dispatcher.sync.ServiceStateChangeReactor;
import dyna.net.security.CredentialManager;
import dyna.net.security.signature.UserSignature;

/**
 * 服务器上下文接口
 * 
 * @author Wanglei
 * 
 */
public interface ApplicationServerContext extends SvContext
{

	/**
	 * 获取服务端配置参数
	 * 
	 * @return
	 */
	public ConfigurableServerImpl getServerConfig();

	/**
	 * 获取license守护
	 * 
	 * @return
	 */
	public LicenseDaemon getLicenseDaemon();

	/**
	 * 获取凭证管理器
	 *
	 * @return
	 */
	public CredentialManager getCredentialManager();

	/**
	 * 获取用户操作日志管理器
	 * 
	 * @return
	 */
	public TrackerManager getTrackerManager();

	/**
	 * 获取多语言管理器
	 * 
	 * @return
	 */
	public NLSManager getNLSManager();

	/**
	 * 设置服务状态变更协调器
	 * 
	 * @param sscReactor
	 *            the sscReactor to set
	 */
	public void setSscReactor(ServiceStateChangeReactor sscReactor);

	/**
	 * 获取服务状态变更协调器
	 * @return
	 */
	public ServiceStateChangeReactor getSscReactor();

	/**
	 * 获取系统用户会话签名
	 * 
	 * @return
	 */
	public UserSignature getSystemInternalSignature();

	/**
	 * 是否调试模式
	 *
	 * @return
	 */
	public boolean isDebugMode();

	/**
	 * 获取会话的最后更新时间,用于缓存,不是从数据库取数据.<br>
	 * 
	 * @param sessionId
	 *            会话id
	 * @param updateTime
	 *            更新时间
	 * @return
	 */
	public boolean shouldUpdateSessionTime(String sessionId, long updateTime);

	/**
	 * 删除缓存内对应会话的最后更新时间, 用户登出系统时调用.
	 * 
	 * @param sessionId
	 */
	public void removeSessionUpdateTime(String sessionId);

}
