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
import dyna.net.service.ApplicationService;

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
public interface Async extends ApplicationService
{

	/**
	 * 保存组织架构关系
	 */
	public void saveGroupTree(Group group, UserSignature userSignature);

	/**
	 * 记录系统操作日志
	 * @param builder
	 * @param signature
	 * @param method
	 * @param parameters
	 * @param result
	 */
	public void systemTrack(TrackerBuilder builder, Signature signature, Method method, Object[] parameters, Object result);

	/**
	 * 删除关联数据
	 * 1、删除end2时，删除结构
	 * 2、删除end1时，删除BOM，View和结构
	 * 3、删除BOM和View时，删除结构
	 *
	 * @param objectGuid
	 * @param exceptionParameter
	 * @throws ServiceRequestException
	 */
	public void deleteReferenceAsync(ObjectGuid objectGuid, String exceptionParameter);

	/**
	 * 查询子阶结构
	 * @param end1
	 * @param templateName
	 * @param searchCondition
	 * @param end2SearchCondition
	 * @param dataRule
	 * @return
	 */
	public CompletableFuture<List<StructureObject>> listObjectOfRelationAsync(ObjectGuid end1, String templateName, SearchCondition searchCondition, SearchCondition end2SearchCondition, DataRule  dataRule);

	/**
	 * 更新是否有end2的标记
	 * @param end1ObjectGuid
	 * @param relationTemplateGuid
	 */
	public void updateHasEnd2Flg(ObjectGuid end1ObjectGuid, String relationTemplateGuid);

	/**
	 * 根据参数查询bom，关联结构
	 * @return
	 */
	public CompletableFuture<AnalysisTask> listBOMAndRelation(ObjectGuid objectGuid, boolean isCheckcl, boolean isbom, boolean isrelation, List<WorkflowTemplateScopeRTInfo> listScopeRT);

	/**
	 * 检查BOM结构，或者普通关联结构是否循环
	 * @return
	 */
	public CompletableFuture<List<ObjectGuid>> checkConnect(ObjectGuid end1, String templateName, boolean isBom);

	/**
	 * 删除取替代数据
	 * @param objectGuid
	 */
	public void deleteReplaceData(ObjectGuid objectGuid, String exceptionParameter, List<FoundationObject> end1List, String bmGuid, boolean deleteReplace);

	/**
	 * 查询bom单层信息
	 * @param end1
	 * @param templateName
	 * @param searchCondition
	 * @param end2SearchCondition
	 * @param dataRule
	 * @return
	 */
	public CompletableFuture<ListBOMTask> listBOM(ObjectGuid end1, String templateName, SearchCondition searchCondition, SearchCondition end2SearchCondition, DataRule dataRule, int level);

	/**
	 * 根据end1将所有模板下的的结构查询出来
	 * @param end1
	 * @param end2SearchCondition
	 * @param dataRule
	 * @return
	 */
	public CompletableFuture<ListBOMTask> listBOMForAllTemplate(ObjectGuid end1, SearchCondition end2SearchCondition, DataRule dataRule);

	/**
	 * 在END1上记录是否存在END2数据，一般用在异步任务中
	 * @param end1List
	 * @param bmGuid
	 * @throws ServiceRequestException
	 */
	public void updateUHasBOM(List<FoundationObject> end1List, String bmGuid) throws ServiceRequestException;

	/**
	 * 刷新文件目录
	 * @param folder
	 * @param isDelete
	 * @param userSignature
	 */
	public void saveFolderTree(Folder folder, boolean isDelete, UserSignature userSignature);

	/**
	 *删除历史查询记录
	 *
	 * @param userGuid
	 */
	public void deleteHistory(String userGuid);

	/**
	 * 保存自定义查询设置
	 * @param condition
	 * @param searchGuid
	 */
	public void saveSearchByCondition(SearchCondition condition, String searchGuid) throws ServiceRequestException;

	/**
	 * 删除项目及其任务
	 */
	public void deleteProject(ObjectGuid projectOObjectGuid);

	/**
	 * 发送邮件
	 * @param mail
	 * @param toUserGuidList
	 * @param languageEnum
	 */
	public void sendMail(Mail mail, List<String> toUserGuidList, LanguageEnum languageEnum);

	/**
	 * 按照类型删除超期对列
	 *
	 * @param jobID
	 */
	public void deleteJobByType(String jobID);

}
