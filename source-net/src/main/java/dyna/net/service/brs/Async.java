package dyna.net.service.brs;

import dyna.common.SearchCondition;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.StructureObject;
import dyna.common.bean.data.structure.BOMStructure;
import dyna.common.bean.signature.Signature;
import dyna.common.bean.sync.AnalysisTask;
import dyna.common.bean.sync.ListBOMTask;
import dyna.common.bean.track.TrackerBuilder;
import dyna.common.dto.DataRule;
import dyna.common.dto.Folder;
import dyna.common.dto.Mail;
import dyna.common.dto.aas.Group;
import dyna.common.dto.template.wft.WorkflowTemplateScopeRTInfo;
import dyna.common.exception.ServiceRequestException;
import dyna.common.systemenum.LanguageEnum;
import dyna.net.security.signature.UserSignature;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 * 多线程服务
 *
 * @author Lizw
 * @date 2022/1/27
 **/
public interface Async
{

	/**
	 * 记录系统操作日志
	 * @param builder
	 * @param signature
	 * @param method
	 * @param parameters
	 * @param result
	 */
	public void systemTrack(TrackerBuilder builder, Signature signature, Method method, Object[] parameters, Object result);


}
