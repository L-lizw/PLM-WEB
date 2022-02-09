package dyna.net.service.brs;

import dyna.common.exception.ServiceRequestException;
import dyna.net.service.Service;

/**
 *
 * 定时任务服务
 *
 * @author Lizw
 * @date 2022/1/29
 **/
public interface ScheduleService extends Service
{
	/**
	 * 检查session过期
	 */
	void checkSession();

	/**
	 * 检查转换队列
	 */
	void checkTransformQueue();

	/**
	 * 邮件清除：按照每个人设置的时间进行清除
	 */
	void clearMail();

	/**
	 * 删除文件传输记录（默认超过一年的）
	 */
	void deleteDSSFileTrans();

	/**
	 * 删除时间超过一年的文件上传记录
	 */
	void fileTransDelete();

	/**
	 * 项目信息计算
	 */
	void projectCalculate();

	/**
	 * 项目预警
	 */
	void projectWarning();

	/**
	 * 执行通知任务
	 * @throws ServiceRequestException
	 */
	void runNoticeAction4Workflow();

	/**
	 * 流程节点超时自动执行
	 */
	void runOverTimeAction();

	/**
	 * 节点超时未执行，代理人邮件通知
	 */
	void runOverTimeAgent();

}
