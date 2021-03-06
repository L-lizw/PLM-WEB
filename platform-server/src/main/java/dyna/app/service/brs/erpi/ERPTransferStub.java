package dyna.app.service.brs.erpi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.Remote;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import dyna.net.service.data.SyncModelService;
import org.jdom.CDATA;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.boas.BOASImpl;
import dyna.app.service.brs.erpi.cross.util.CrossConfigureManager;
import dyna.app.service.brs.erpi.cross.util.XMLUtil;
import dyna.app.service.brs.erpi.dataExport.IntegrateERP;
import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.bean.data.SystemObject;
import dyna.common.bean.erp.ERPQuerySchemaParameterObject;
import dyna.common.bean.erp.ERPSchema;
import dyna.common.dto.BooleanResult;
import dyna.common.dto.Queue;
import dyna.common.dto.erp.CrossIntegrate;
import dyna.common.dto.erp.CrossServiceConfig;
import dyna.common.dto.erp.ERPMoreCompanies;
import dyna.common.dto.erp.ERPServiceConfig;
import dyna.common.dto.erp.ERPTransferLog;
import dyna.common.dto.erp.tmptab.ERPTempTableInfo;
import dyna.common.exception.ServiceRequestException;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.ERPServerType;
import dyna.common.systemenum.FieldTypeEnum;
import dyna.common.systemenum.LanguageEnum;
import dyna.common.util.Base64Util;
import dyna.common.util.DateFormat;
import dyna.common.util.EncryptUtils;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import dyna.common.util.UpdatedECSConstants;
import dyna.net.service.das.MSRM;

/**
 * ????????????Cross?????????PLM???????????????PLM???webservice?????????webservice????????????Cross??? <br/>
 * ?????????Cross??????PLM????????????ERP webservice
 * 
 * @author chega
 * 
 *         2013-1-7??????11:41:19
 * 
 */
public abstract class ERPTransferStub<T extends Enum<?>> extends AbstractServiceStub<ERPIImpl>
{
	public static final String			NO_DATA_SENT			= "No Data to be sent";
	public static final String			DATA_HAS_SENT			= "The Data is not changed and has been sent";
	public static final String			NO_DATA_RECEIVED		= "No Data was received";
	/**
	 * ??????????????????(s)
	 */
	protected int						operationPollingTime	= 20;
	/**
	 * ????????????Operation??????????????????
	 */
	protected int						operationLiveTime		= 300;
	/**
	 * Operation????????????
	 */
	private boolean						timeOver				= false;
	private boolean						timerRunning			= false;
	private TimeCounter					timer					= null;
	/**
	 * ?????????
	 */
	protected IntegrateERP<T>			integrate;
	protected StringBuilder				exportResult			= new StringBuilder();
	protected String					exportSuccess;
	public String						jobId					= "";
	public String						userId					= "";
	public String						factory					= "";
	public ERPSchema					schema;
	public LanguageEnum					lang;
	public ERPServerType				ERPType;
	public String						templateName;
	public ObjectGuid					objectGuid;
	public FoundationObject				foundationObject;
	public boolean						useCross				= false;
	public ERPServiceConfig				ERPConfig;
	/**
	 * ?????????????????????Operation index(???????????????0)
	 */
	public int							currentOpeIdx			= -1;
	/**
	 * ?????????????????????XML??????(???????????????1)
	 */
	public int							currentXMLPackIdx		= 0;
	private static final DecimalFormat	TRIPLE_DIGIT_FORMATTER	= new DecimalFormat("000");
	public static final String			LINE_SEPERATOR			= System.getProperty("line.separator");

	protected Remote					stub;
	public int							seq_sub_cnt				= 0;

	public CrossIntegrate				export2Server			= null;
	// tempTableName
	public String						tempTableName			= null;

	private CrossServiceConfig			crossConfig;

	public String						jobGUID;

	public Queue						job;																	// for_test
																												// TODO

	public MSRM							msrm					= null;


	/**
	 * ???ERP?????????????????????????????????????????????????????????????????????destroy()?????????????????? <br/>
	 * ????????????????????????????????????????????????????????????????????????????????????null??? <br/>
	 * ?????????????????????<b>????????????????????????</b>???:<br/>
	 * &nbsp;&nbsp;1:?????????????????????????????????????????????????????????????????????????????????????????????????????????restrictions?????????<br/>
	 * &nbsp;&nbsp;2:????????????????????????????????????????????????????????????????????????<br/>
	 * ????????????????????????????????????????????????????????????????????????????????????; ?????????????????????????????????ERP?????????
	 * 
	 * <br/>
	 * <br/>
	 * ???????????????????????????????????????IllegalArgumentException?????????????????????????????? <br/>
	 * ??????ERPTransferLog?????????????????????????????????<b>dataMap.get(category)</b>??????size????????????????????????????????????????????? <br/>
	 * ????????????????????????dataMap.get(category)?????????????????????List?????????category??????????????????RecordSet??????tobeExportedGuidMap????????????category?????????????????????List???
	 * <p/>
	 * ??????????????????????????????PLM?????????????????????ERP???????????????????????????ERP???????????????????????????PLM??????????????????????????????????????????XML?????????(XMLPackageNo)?????????
	 * ????????????????????????xml????????????????????????(?????????PLM?????????????????????xml??????)???<br/>
	 * ????????????????????????????????????????????????xml???????????????????????????????????????????????????xml???
	 * 
	 * @param objectGuid
	 * @param serviceConfig
	 * @return
	 * @throws Exception
	 *             <p/>
	 *             dyna.common.bean.data.system.Queue ???db????????????????????????
	 *             <table border="1">
	 *             <tr>
	 *             <th>Queue Field</th>
	 *             <th>ERP Field</th>
	 *             </tr>
	 *             <tr>
	 *             <td>FieldD</td>
	 *             <td>schemaName</td>
	 *             </tr>
	 *             <tr>
	 *             <td>FieldE</td>
	 *             <td>BOMTemplateName</td>
	 *             </tr>
	 *             <tr>
	 *             <td>FieldG</td>
	 *             <td>lang</td>
	 *             </tr>
	 *             <tr>
	 *             <td>FieldF</td>
	 *             <td>factory</td>
	 *             </tr>
	 *             <tr>
	 *             <td>FieldH</td>
	 *             <td>jobId</td>
	 *             </tr>
	 *             </table>
	 * 
	 *      <p/>
	 */
	public BooleanResult export(ObjectGuid objectGuid, Queue jobQueue, String uerId, ERPServiceConfig serviceConfig) throws Exception
	{
		if (!StringUtils.isNullString(uerId))
		{
			this.userId = uerId;
		}
		BooleanResult finalResult = null;
		try
		{
			this.beforeExport(objectGuid, jobQueue, this.userId, serviceConfig);
			long start = System.currentTimeMillis();
			this.integrate.getAllDataWithChildren(this.foundationObject, null);
			long end = System.currentTimeMillis();
			DynaLogger.debug("dyna.app.service.brs.erpi.dataExport.IntegrateERP#generateAllData time consumed: " + (end - start) + "ms");
			if (!this.integrate.getDataErrorList().isEmpty())
			{
				return new BooleanResult(false, this.integrate.printDataErrorList());
			}
			this.integrate.fillExportGuidMap();
			start = System.currentTimeMillis();
			final BooleanResult result = this.doExportWork();
			finalResult = new BooleanResult(result.getFlag());
			finalResult.setDataEmpty(result.isDataEmpty());
			finalResult.setDetail(this.exportResult.toString() + ERPTransferStub.LINE_SEPERATOR);
			end = System.currentTimeMillis();
			DynaLogger.debug("dyna.app.service.brs.erpi.dataExport.IntegrateERP#exportData2ERP time consumed: " + (end - start) + "ms");
			return finalResult;
		}
		catch (Exception t)
		{
			DynaLogger.error(t);
			String msgId = null;
			if (t instanceof ServiceRequestException)
			{
				msgId = ((ServiceRequestException) t).getMsrId();
				throw new ServiceRequestException(msgId, this.exportResult.toString(), t, ((ServiceRequestException) t).getArgs());
			}
			else
			{
				throw new ServiceRequestException(msgId, this.exportResult.toString(), t);
			}

		}
		finally
		{
			this.afterExport();
		}
	}

	public BooleanResult exportMergeData(List<ObjectGuid> objectGuids, Queue queuejob, String userId, ERPServiceConfig serviceConfig) throws ServiceRequestException
	{
		long start;
		long end;
		if (!StringUtils.isNullString(userId))
		{
			this.userId = userId;
		}
		BooleanResult finalResult = null;
		try
		{
			if (!SetUtils.isNullList(objectGuids))
			{
				this.integrate.beforeProcessDataList4Merge();
				for (ObjectGuid oGuid : objectGuids)
				{
					this.beforeExport(oGuid, queuejob, this.userId, serviceConfig);
					start = System.currentTimeMillis();
					this.integrate.getAllMergeDataWithChildren(this.foundationObject, null);
					end = System.currentTimeMillis();
					DynaLogger.debug("dyna.app.service.brs.erpi.dataExport.IntegrateERP#generateAllData time consumed: " + (end - start) + "ms");
					if (!this.integrate.getDataErrorList().isEmpty())
					{
						return new BooleanResult(false, this.integrate.printDataErrorList());
					}
					this.integrate.fillExportGuidMap();
				}
			}
			start = System.currentTimeMillis();
			final BooleanResult result = this.doExportWork();
			finalResult = new BooleanResult(result.getFlag());
			finalResult.setDataEmpty(result.isDataEmpty());
			finalResult.setDetail(this.exportResult.toString() + ERPTransferStub.LINE_SEPERATOR);
			end = System.currentTimeMillis();
			DynaLogger.debug("dyna.app.service.brs.erpi.dataExport.IntegrateERP#exportData2ERP time consumed: " + (end - start) + "ms");
			return finalResult;
		}
		catch (Exception t)
		{
			DynaLogger.error(t);
			String msgId = null;
			if (t instanceof ServiceRequestException)
			{
				msgId = ((ServiceRequestException) t).getMsrId();
				throw new ServiceRequestException(msgId, this.exportResult.toString(), t, ((ServiceRequestException) t).getArgs());
			}
			else
			{
				throw new ServiceRequestException(msgId, this.exportResult.toString(), t);
			}

		}
		finally
		{
			this.afterExport();
		}
	}

	/**
	 * ?????????
	 * 
	 * @param schemaName
	 * @param lang
	 * @param userId
	 * @param factory
	 * @param jobId
	 * @param serviceConfig
	 * @throws Exception
	 */
	protected void beforeExport(ObjectGuid objectGuid, Queue jobQueue, String userId, ERPServiceConfig serviceConfig) throws Exception
	{
		this.decorateServieConfig(serviceConfig);
		boolean isEC = UpdatedECSConstants.ECN.equals(jobQueue.getFieldi()) || UpdatedECSConstants.ECO.equals(jobQueue.getFieldi());
		String ECChangeType = isEC ? jobQueue.getFieldj() : null;
		ERPQuerySchemaParameterObject schemaParameterObj = new ERPQuerySchemaParameterObject();
		schemaParameterObj.setECChangeType(ECChangeType);
		schemaParameterObj.setERPType(this.ERPType);
		schemaParameterObj.setEC(isEC);
		schemaParameterObj.setSchemaName(jobQueue.getFieldd());
		schemaParameterObj.setBomChange("Y".equals(jobQueue.getFieldk()));
		this.schema = this.stubService.getSchemaByName(schemaParameterObj);
		if (this.schema == null)
		{
			throw new IllegalArgumentException("schema not found: " + jobQueue.getFieldd());
		}
		if (this.schema.getOperationList().isEmpty())
		{
			throw new IllegalArgumentException("no include attribute for " + this.schema.getName() + " in " + this.integrate.getXMLPath());
		}
		this.templateName = jobQueue.getFielde();
		this.userId = userId == null ? "" : userId;
		this.factory = jobQueue.getFieldf();
		this.jobId = jobQueue.getFieldh();
		this.lang = LanguageEnum.getById(jobQueue.getFieldg());
		this.useCross = this.isUseCross(serviceConfig);
		this.foundationObject = ((BOASImpl) this.stubService.getBOAS()).getFoundationStub().getObject(objectGuid, false);
		this.exportSuccess = this.stubService.getMSRM().getMSRString("ID_APP_INTEGRATEWF_ERP_DATA_SUCESS", this.lang.toString());
		this.jobGUID = jobQueue.getGuid();
		this.job = jobQueue;// TODO test
		msrm = this.stubService.getMSRM();
		if (this.schema == null)
		{
			throw new IllegalArgumentException("schema not found: " + jobQueue.getFieldd());
		}
	}

	/**
	 * ?????????????????????
	 */
	protected void afterExport()
	{
		DynaLogger.debug("transfer over, destroy resource");
		this.objectGuid = null;
		this.foundationObject = null;
		this.schema = null;
		this.templateName = null;
		this.currentOpeIdx = -1;
		this.ERPConfig = null;
		this.ERPType = null;
		this.factory = null;
		this.jobId = null;
		this.lang = null;
		this.useCross = false;
		this.userId = null;
		this.stub = null;
		this.crossConfig = null;
		this.integrate.releaseResource();
		this.integrate = null;
	}

	/**
	 * ????????????ERP
	 * some ERPs just do after receiving data, like workflow & E10, some may wait until get all xml packages like YT,
	 * <br/>
	 * and YF is special, it writes back a file to tell PLM the processing result.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected BooleanResult doExportWork() throws Exception
	{
		String operationName = null;
		Map<String, Boolean> operationDataMap = null;
		String category = null;
		try
		{
			checkServer();
			checkTempTable();
			operationDataMap = this.integrate.getOperationDataEmptyFlag(false, null);

			long start = System.currentTimeMillis();
			this.integrate.compareWithDB(this.schema.isExportAllData());
			long end = System.currentTimeMillis();
			DynaLogger.info("dyna.app.service.brs.erpi.dataExport.IntegrateERP#compareWithDB time consumed: " + (end - start) + "ms");

			operationDataMap = this.integrate.getOperationDataEmptyFlag(true, operationDataMap);
			List<T> operationList = this.integrate.getOperationList(true);
			for (int i = 0; i < operationList.size(); i++)
			{
				this.startCount();
				this.currentOpeIdx = i;
				T operation = operationList.get(i);
				operationName = this.integrate.getOperationName(operation);
				category = this.integrate.getOperationCategory(operation);
				int totalCount = this.integrate.getXMLPackageCount(operation);
				if (totalCount <= 0)
				{
					if (operationDataMap.get(category))
					{
						this.appendResult(this.integrate.getOperationName(operation), NO_DATA_SENT, false);
					}
					else
					{
						this.appendResult(this.integrate.getOperationName(operation), DATA_HAS_SENT, false);
					}
					continue;
				}
				BooleanResult exportResult = this.exportOpeation(operation, totalCount);
				String detailMsg = !StringUtils.isNullString(exportResult.getDetail()) ? "ERP:    " + exportResult.getDetail() : null;
				exportResult.setDetail(detailMsg);
				// ??????:??????????????????:??????????????????,???????????????ERP???????????????????????????ERP??????????????????????????????????????????E10???WF????????????PLM??????prepare4NextOperation????????????????????????
				// ??????ERP???????????????????????????????????????????????????????????????????????????PLM?????????????????????????????????(???????????????????????????????????????prepare4NextOperation.
				// ??????exportOperation()??????true?????????????????????
				if (exportResult.getFlag())
				{
					BooleanResult prepareResult = this.prepare4NextOperation(operation);
					// ??????prepare4NextOperation??????false??????????????????????????????ERP???????????????
					if (!prepareResult.getFlag())
					{
						this.appendResult(operationName, prepareResult.getDetail(), false);
						return prepareResult;
					}
					else
					{
						// ??????prepare4NextOperation??????true????????????prepare4NextOperation????????????????????????????????????????????????exportOperation?????????
						final String returnResult = StringUtils.isNullString(prepareResult.getDetail()) ? exportResult.getDetail() : prepareResult.getDetail();
						this.appendResult(operationName, returnResult, false);
						// write log into database
						this.saveTransferStatus(this.integrate.getOperationCategory(operation), -1, this.integrate.getOperationId(operation));
						continue;
					}
				}
				else
				{
					this.appendResult(operationName, exportResult.getDetail(), true);

					return exportResult;
				}
			}

			// ??????for??????????????????????????????Operation?????????????????????????????????????????????????????????true
			return BooleanResult.getNull();
		}
		catch (Exception e)
		{
			String msg = e.getMessage();
			if (msg == null)
			{
				msg = e.getCause() == null ? StringUtils.EMPTY_STRING : e.getCause().getMessage();
			}

			this.appendResult(operationName, msg, false);

			throw e;
		}
	}

	/**
	 * ?????????Operation????????????
	 * 
	 * @param operation
	 * @param count
	 *            the total count of xml packages
	 * @return
	 * 		transfer the xml data to ERP
	 * @throws Exception
	 * 
	 * @see #prepare4NextOperation
	 */
	protected BooleanResult exportOpeation(T operation, int count) throws Exception
	{
		String returnString = null;
		for (int j = 1; j <= count; j++)
		{
			this.checkTimeOut();
			this.currentXMLPackIdx = j;
			Document document = this.integrate.getCreateDataXML(operation, count, j);
			String dataString = XMLUtil.convertXML2String(document, this.isOmitDeclarationInXML(), this.isExpandEmptyTagInXML());
			// set document to null, convenient for GC
			document = null;
			returnString = this.callWS(dataString, operation);
			BooleanResult result = this.parseResponseString(returnString);
			if (result.getFlag())
			{
				continue;
			}
			else
			{
				// ????????????
				return result;
			}
		}
		return new BooleanResult(true, this.exportSuccess);
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @return
	 * @throws Exception
	 */
	protected BooleanResult doExportWork4CF() throws Exception
	{
		String operationName = null;
		try
		{
			List<T> operationList = this.integrate.getOperationList(false);
			for (int i = 0; i < operationList.size(); i++)
			{
				this.startCount();
				this.currentOpeIdx = i;
				T operation = operationList.get(i);
				operationName = this.integrate.getOperationName(operation);
				int totalCount = this.integrate.getXMLPackageCount(operation);
				if (totalCount <= 0)
				{
					this.appendResult(this.integrate.getOperationName(operation), NO_DATA_SENT, false);
					continue;
				}
				BooleanResult exportResult = this.exportOpeation(operation, totalCount);
				String detailMsg = !StringUtils.isNullString(exportResult.getDetail()) ? "ERP:    " + exportResult.getDetail() : null;
				exportResult.setDetail(detailMsg);
				// ??????:??????????????????:??????????????????,???????????????ERP???????????????????????????ERP??????????????????????????????????????????E10???WF????????????PLM??????prepare4NextOperation????????????????????????
				// ??????ERP???????????????????????????????????????????????????????????????????????????PLM?????????????????????????????????(???????????????????????????????????????prepare4NextOperation.
				// ??????exportOperation()??????true?????????????????????
				if (exportResult.getFlag())
				{
					BooleanResult prepareResult = this.prepare4NextOperation(operation);
					// ??????prepare4NextOperation??????false??????????????????????????????ERP???????????????
					if (!prepareResult.getFlag())
					{
						this.appendResult(operationName, prepareResult.getDetail(), false);
						return prepareResult;
					}
					else
					{
						// ??????prepare4NextOperation??????true????????????prepare4NextOperation????????????????????????????????????????????????exportOperation?????????
						final String returnResult = StringUtils.isNullString(prepareResult.getDetail()) ? exportResult.getDetail() : prepareResult.getDetail();
						this.appendResult(operationName, returnResult, false);
						// write log into database
						this.saveTransferStatus(this.integrate.getOperationCategory(operation), -1, this.integrate.getOperationId(operation));
						continue;
					}
				}
				else
				{
					this.appendResult(operationName, exportResult.getDetail(), true);

					return exportResult;
				}
			}

			// ??????for??????????????????????????????Operation?????????????????????????????????????????????????????????true
			return BooleanResult.getNull();
		}
		catch (Exception e)
		{
			String msg = e.getMessage();
			if (msg == null)
			{
				msg = e.getCause() == null ? StringUtils.EMPTY_STRING : e.getCause().getMessage();
			}

			this.appendResult(operationName, msg, false);

			throw e;
		}
	}

	/**
	 * ?????????Operation?????????????????????????????????????????????Operation????????????????????????????????????Operation???????????????<br/>
	 * ????????????true
	 * 
	 * @param operation
	 *            ??????????????????????????????????????????????????????????????????????????????????????????????????????
	 * @throws Exception
	 */
	protected BooleanResult prepare4NextOperation(T operation) throws Exception
	{
		return BooleanResult.getNull();
	}

	/**
	 * ??????webservice????????????<b>xml</b>??????ERP
	 * 
	 * @param xml
	 * @param operation
	 * @return
	 * @throws Exception
	 */
	public String callWS(String originalXML, T operation) throws Exception
	{
		String returnString = null;
		if (useCross)
		{
			CrossServiceConfig crossServiceConfig = CrossConfigureManager.getInstance().getCrossServiceConfig();
			originalXML = this.wrapWithCross(crossServiceConfig, originalXML, operation);
			this.saveTempFile(this.getXMLPackNo() + "_request.xml", originalXML);
			String crossReturnString = (String) stubService.getCrossStub().crossExportDataToErp(originalXML);
			this.saveTempFile(this.getXMLPackNo() + "_response.xml", crossReturnString);
			returnString = this.unwrapWithCross(crossReturnString, crossServiceConfig.getHostIsEncode());
			if (StringUtils.isNullString(returnString))
			{
				throw new IllegalArgumentException(this.parseCrossErrorString(crossReturnString));
			}
		}
		else
		{
			returnString = this.doCallWS(originalXML, operation);
		}
		return returnString;
	}

	public String checkConnectionCallWS(String originalXML, T operation) throws Exception
	{
		String returnString = null;
		if (useCross)
		{
			CrossServiceConfig crossServiceConfig = CrossConfigureManager.getInstance().getCrossServiceConfig();
			originalXML = this.wrapWithCross(crossServiceConfig, originalXML, operation);
			this.saveTempFile(System.nanoTime() + "_request.xml", originalXML);
			String crossReturnString = (String) stubService.getCrossStub().crossExportDataToErp(originalXML);
			this.saveTempFile(System.nanoTime() + "_response.xml", crossReturnString);
			returnString = this.unwrapWithCross(crossReturnString, crossServiceConfig.getHostIsEncode());
			if (StringUtils.isNullString(returnString))
			{
				throw new IllegalArgumentException(this.parseCrossErrorString(crossReturnString));
			}
		}
		else
		{
			returnString = this.checkConnectionDoCallWS(originalXML, operation);
		}
		return returnString;
	}

	/**
	 * ??????webservice
	 * 
	 * @param originXML
	 * @param operation
	 * @return
	 * @throws Exception
	 */
	protected abstract String doCallWS(String originXML, T operation) throws Exception;

	protected abstract String checkConnectionDoCallWS(String originXML, T operation) throws Exception;

	/**
	 * ??????????????????
	 * 
	 * @param category
	 * @param count
	 *            ????????????????????????????????????????????????????????????-1??????????????? <br/>
	 *            ???????????????????????????Operation????????????????????????count==-1
	 * @throws ServiceRequestException
	 */
	protected void saveTransferStatus(String category, int count, String operationId) throws ServiceRequestException
	{
		// ???????????????????????????????????????????????????
		if (!this.integrate.isSaveTransferStatus())
		{
			return;
		}
		// ?????????????????????BOM???????????????
		if (!(IntegrateERP.ERP_ITEM.equals(category) || IntegrateERP.ERP_BOM.equals(category)))
		{
			return;
		}
		int saveCount = count;
		if (count == -1)
		{
			saveCount = this.integrate.getExportedGuidMap().get(category).size();
		}
		int i = 0;
		while (i < saveCount)
		{
			ObjectGuid objectGuid = this.integrate.getExportedGuidMap().get(category).removeFirst();
			ERPTransferLog log = new ERPTransferLog();
			log.setTargetGuid(objectGuid.getGuid());
			log.setIteration(((FoundationObject) this.integrate.getObject(objectGuid)).getIterationId());
			log.setCategory(category);
			log.setERPFactory(this.factory);
			log.setERPName(this.ERPType.getProName());
			log.setERPOperation(operationId);
			log.put(SystemObject.CREATE_USER_GUID, this.stubService.getUserSignature().getUserGuid());
			this.stubService.saveERPTransferLog(log);
			i++;
		}
	}

	/**
	 * ??????cross?????????????????????
	 * 
	 * @param returnString
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 * @throws Exception
	 */
	protected String parseCrossErrorString(String returnString) throws JDOMException, IOException
	{
		Element root = XMLUtil.convertString2XML(returnString).getRootElement();
		Element resultElement = root.getChild("code");
		String result = resultElement.getTextTrim();
		Element messageElment = root.getChild("message");
		String message = messageElment.getTextTrim();
		return message + "(" + result + ")";
	}

	/**
	 * ??????ERP????????????????????????CROSS?????????
	 * 
	 * @param responseString
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	protected abstract BooleanResult parseResponseString(String responseString) throws JDOMException, IOException;

	/**
	 * ????????????cross??????
	 * 
	 * @return
	 */
	protected final boolean isUseCross(ERPServiceConfig serviceConfig)
	{
		return "Y".equalsIgnoreCase(serviceConfig.isCrossIntegrate());
	}

	/**
	 * cross header???????????????&lt;param>??????
	 * 
	 * @return
	 */
	protected boolean containParamTagInCross()
	{
		return true;
	}

	/**
	 * ???<b>originalXML</b>??????cross??? <br/>
	 * ERPTransferStub??????CROSS????????????????????????CrossStub???????????????????????????????????????CrossStub???????????????????????????ERPTransferStub?????????????????????????????????ERPTransferStub??????
	 * ???
	 * 
	 * @return
	 * @throws IOException
	 */
	protected String wrapWithCross(CrossServiceConfig crossConfig, String originalXML, T operation) throws IOException
	{
		Element host = this.makeHost(crossConfig, this.userId);
		Element service = this.makeService(crossConfig, operation);
		String key = this.encryptKeyMD5(host, service);
		Element payloadEle = null;
		if (this.containParamTagInCross())
		{
			payloadEle = new Element("payload").addContent(new Element("param").setAttribute("key", "Data").setAttribute("type", "String").addContent(new CDATA(originalXML)));
		}
		else
		{
			payloadEle = new Element("payload").addContent(new CDATA(originalXML));
		}
		Element requestEle = new Element("request").setAttribute("type", "sync").setAttribute("key", key).addContent(host).addContent(service).addContent(this.makeDataKey())
				.addContent(payloadEle);

		String requestStr = XMLUtil.convertXML2String(requestEle);
		if (crossConfig.getHostIsEncode())
		{
			requestStr = Base64Util.encryptBase64(requestStr);
		}

		return requestStr;
	}

	/**
	 * ???cross?????????<b>responseXML</b>??????
	 * 
	 * @param responseXML
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	protected String unwrapWithCross(String responseXML, boolean isEncoding) throws JDOMException, IOException
	{
		DynaLogger.debug(responseXML);
		if (StringUtils.isNullString(responseXML))
		{
			throw new IllegalArgumentException(responseXML + " returned from CROSS(ERP) is not a valid XML Document");
		}
		String response = null;
		if (isEncoding)
		{
			response = Base64Util.decodeBase64(responseXML);
		}
		else
		{
			response = responseXML;
		}
		Element root = XMLUtil.convertString2XML(response).getRootElement();
		if (root.getChild("payload") == null)
		{
			throw new IllegalArgumentException(response + " does not contain <payload> tag which is must.");
		}
		Iterator<Content> it = null;
		if (this.containParamTagInCross())
		{
			if (SetUtils.isNullList(root.getChild("payload").getContent()))
			{
				throw new IllegalArgumentException(" returned from CROSS(ERP):" + root.getChildText("message"));
			}
			else if (root.getChild("payload").getChild("param") == null)
			{
				throw new IOException(((CDATA) root.getChild("payload").getContent().get(0)).getText());
			}
			it = root.getChild("payload").getChild("param").getContent().iterator();
		}
		else
		{
			it = root.getChild("payload").getContent().iterator();
		}
		while (it.hasNext())
		{
			Content cont = it.next();
			if (cont instanceof CDATA)
			{
				return ((CDATA) cont).getText();
			}
		}
		return root.getChildText("message");
	}

	protected Element makeHost(CrossServiceConfig crossServiceConfig, String loginUserId)
	{
		Element hostEle = new Element("host").setAttribute("prod", StringUtils.convertNULLtoString((CrossServiceConfig.PRODUCT_NAME)))
				.setAttribute("ver", StringUtils.convertNULLtoString(crossServiceConfig.getHostVer()))
				.setAttribute("ip", StringUtils.convertNULLtoString(crossServiceConfig.getHostIP())).setAttribute("acct", StringUtils.convertNULLtoString(loginUserId))
				.setAttribute("timestamp", DateFormat.format(new Date(), DateFormat.PTN_TIMESTAMP))
				.setAttribute("id", StringUtils.convertNULLtoString(crossServiceConfig.getHostID()));
		return hostEle;
	}

	/**
	 * ?????????md5???
	 * 
	 * @param crossM
	 * @return
	 * @throws IOException
	 */
	protected String encryptKeyMD5(Element host, Element service) throws IOException
	{
		String tmp = XMLUtil.convertXML2String(host) + XMLUtil.convertXML2String(service);
		String encryptMD5 = EncryptUtils.encryptMD5(tmp + CrossStub.CROSS_SALT);
		return encryptMD5;
	}

	protected Element makeDataKey()
	{
		Element dataKeyEle = new Element("datakey").setAttribute("type", "FOM").addContent(new Element("key").setAttribute("name", "CompanyId").setText("Digiwin"))
				.addContent(new Element("key").setAttribute("name", "Product").setText(CrossStub.PLM_NAME));
		return dataKeyEle;
	}

	protected Element makeService(CrossServiceConfig crossServiceConfig, T operation)
	{
		Element serviceEle = new Element("service");
		List<CrossIntegrate> servers = new ArrayList<CrossIntegrate>();
		servers = crossServiceConfig.getServices();
		boolean crossConfigured = false;
		if (!SetUtils.isNullList(servers))
		{
			for (CrossIntegrate crossServer : servers)
			{
				if (this.getERPTypeNameAsList().contains(crossServer.getServiceProd()))
				{
					serviceEle.setAttribute("prod", StringUtils.convertNULLtoString(crossServer.getServiceProd()))
							.setAttribute("ip", StringUtils.convertNULLtoString(crossServer.getServiceIP()))
							.setAttribute("name", this.integrate.getOperationCrossServiceName(operation))
							.setAttribute("id", StringUtils.convertNULLtoString(crossServer.getServiceID()));
					crossConfigured = true;
					break;
				}
			}
		}
		if (!crossConfigured)
		{

			msrm = this.stubService.getMSRM();

			throw new IllegalArgumentException(msrm.getMSRString("ID_APP_ERPI_CROSS_SYNCHRONIZE", lang.toString()));
		}
		return serviceEle;
	}

	/**
	 * PLM???ERP??????????????????????????????
	 * (SM???????????????????????????)
	 * 
	 * @param list
	 * @param serviceConfig
	 * @param factoryId
	 * @return
	 * @throws Exception
	 */
	public List<FoundationObject> getInfoFromERP(List<FoundationObject> list, ERPServiceConfig serviceConfig, List<String> factoryId) throws Exception
	{
		List<String> contentList = new ArrayList<String>();
		List<String> returnStringList = null;
		List<String> paramList = new ArrayList<String>();
		List<BooleanResult> resultList = new ArrayList<BooleanResult>();
		List<FoundationObject> returnList = new ArrayList<FoundationObject>();
		ERPServerType type = null;
		this.lang = this.stubService.getUserSignature().getLanguageEnum();
		this.decorateServieConfig(serviceConfig);
		type = ERPServerType.valueOf(serviceConfig.getERPServerSelected());
		ERPQuerySchemaParameterObject parameterObject = new ERPQuerySchemaParameterObject();
		parameterObject.setSchemaName(serviceConfig.getSchemaName());
		parameterObject.setERPType(type);
		parameterObject.setECChangeType(null);
		parameterObject.setEC(false);
		this.schema = this.stubService.getSchemaByName(parameterObject);
		this.currentXMLPackIdx = 1;
		this.jobId = String.valueOf(System.nanoTime());
		this.currentOpeIdx = 1;

		contentList = schema.getContentList();
		this.useCross = this.isUseCross(serviceConfig);
		if (this.checkConfigFile(type))
		{
			String message = this.stubService.getERPStub().getFileName(type) + ".xml";
			message += this.stubService.getMSRM().getMSRString("ID_APP_ERPI_CONFIG_SCHEMA_ERROR", this.lang.toString()) + ":";
			message += this.schema.getName();
			throw new ServiceRequestException(message);
		}
		returnStringList = this.integrate.getReturnList(list, factoryId, serviceConfig, contentList);
		if (!SetUtils.isNullList(returnStringList))
		{
			for (String returnString : returnStringList)
			{
				BooleanResult result = this.parseResponseString(returnString);
				resultList.add(result);
			}
		}
		if (!SetUtils.isNullList(resultList))
		{
			StringBuffer messageBuff = new StringBuffer();
			for (BooleanResult result : resultList)
			{
				if (!result.getFlag())
				{
					if (messageBuff.length() > 0)
					{
						messageBuff.append(";");
					}
					messageBuff.append(result.getDetail());
					continue;
				}
				paramList.add(result.getDetail());
			}
			if (messageBuff.length() > 0)
			{
				throw new ServiceRequestException(messageBuff.toString());
			}
		}
		returnList = this.integrate.getReturnList(paramList, contentList);
		this.afterExport();
		return returnList;
	}

	/**
	 * ???ERP???????????????????????????????????????
	 * 
	 * @param type
	 * @return
	 */
	protected boolean checkConfigFile(ERPServerType type)
	{
		if (this.schema == null || SetUtils.isNullList(this.schema.getOperationList()) || SetUtils.isNullList(this.schema.getContentList()))
		{
			return true;
		}
		List<String> operationList = this.schema.getOperationList();
		List<String> contentList = this.schema.getContentList();
		for (String operation : operationList)
		{
			Map<String, String> map = this.stubService.getERPStub().getOperationAttribute(type, operation);
			if (map.isEmpty())
			{
				return true;
			}
		}
		for (String content : contentList)
		{
			Map<String, String> map = this.stubService.getERPStub().getContentAttribute(type, content);
			if (map.isEmpty())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * ????????????ERPType?????????,???????????????????????????Name
	 * 
	 * @return
	 */
	protected List<String> getERPTypeNameAsList()
	{
		return Arrays.asList(this.ERPType.getProName());
	}

	public String decodeString(String str, boolean isEncoding)
	{
		String response = null;
		if (isEncoding)
		{
			response = Base64Util.decodeBase64(str);
		}
		else
		{
			response = str;
		}
		return response;
	}

	public ERPIImpl getStubService()
	{
		return this.stubService;
	}

	protected abstract Remote getRemoteStub() throws MalformedURLException, ServiceException;

	/**
	 * ???conf/crossconf.xml?????????cross?????????
	 * 
	 * @return
	 */
	protected CrossServiceConfig getCrossConfig()
	{
		if (this.crossConfig == null)
		{
			this.crossConfig = CrossConfigureManager.getInstance().getCrossServiceConfig();
		}
		return this.crossConfig;
	}

	/**
	 * ????????????UI?????????
	 * 
	 * @param serviceConfig
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	public BooleanResult checkConnection(ERPServiceConfig serviceConfig) throws Exception
	{
		this.decorateServieConfig(serviceConfig);
		if (!serviceConfig.getErpServerAddress().matches("http[s]?://(\\d{1,3}\\.){3}\\d{1,3}.+[^(?i)(\\?wsdl)]*"))
		{
			throw new IllegalArgumentException("address is not valid");
		}
		URLConnection conn = new URL(serviceConfig.getErpServerAddress() + "?wsdl").openConnection();
		String type = conn.getContentType();
		if (type == null)
		{
			return new BooleanResult(false, "");
		}
		if (type.contains("text/xml"))
		{
			return new BooleanResult(true, "");
		}
		else
		{
			return new BooleanResult(false, "");
		}
	}

	/**
	 * ???????????????
	 */
	protected void startCount()
	{
		if (this.operationLiveTime <= 0)
		{
			throw new IllegalArgumentException("operationLiveTime must be greater than 0");
		}
		this.timeOver = false;
		this.timerRunning = true;
		this.timer = new TimeCounter();
		this.timer.setDaemon(true);
		this.timer.start();
	}

	/**
	 * ???????????????
	 */
	private void stopCount()
	{
		if (this.timer == null)
		{
			return;
		}
		this.timerRunning = false;
		try
		{
			this.timer.join();
		}
		catch (InterruptedException e)
		{
			DynaLogger.error("TimerCount is interrupted. " + this.getClass().getName() + "#stopCount");
		}
	}

	/**
	 * ??????JobId??????datakey?????????${JobId} + ???????????????
	 * 
	 * @return
	 */
	public String getDataKey()
	{
		if (this.jobId == null || this.currentOpeIdx < 0)
		{
			throw new IllegalArgumentException("argument is invalid: jobId=" + this.jobId + ", currentOpeIdx=" + this.currentOpeIdx);
		}
		return this.jobId.concat(TRIPLE_DIGIT_FORMATTER.format(this.currentOpeIdx + 1));
	}

	/**
	 * ???????????????????????????XML???ID?????????${DataKey}+???????????????
	 * 
	 * @see #getDataKey
	 * @return
	 */
	public String getXMLPackNo()
	{
		if (this.currentXMLPackIdx < 1)
		{
			throw new IllegalArgumentException("argument is invalid: currentXMLPackNo=" + this.currentXMLPackIdx);
		}
		return this.getDataKey().concat(TRIPLE_DIGIT_FORMATTER.format(this.currentXMLPackIdx));
	}

	/**
	 * ????????????????????????
	 */
	protected void checkTimeOut()
	{
		if (this.timeOver)
		{
			throw new TimeoutException("time out, exceeds " + this.operationLiveTime + "s");
		}
	}

	protected void sleep(int i)
	{
		if (i <= 0)
		{
			return;
		}
		try
		{
			Thread.sleep(i * 1000);
		}
		catch (InterruptedException e)
		{
			DynaLogger.info(this.getClass().getName() + " sleep fail: " + e.getMessage());
		}
	}

	/**
	 * ???<b>operation</b>?????????????????????????????????????????? <br/>
	 * ??????????????????????????????????????????
	 * 
	 * @param operation
	 * @param msg
	 */
	protected void appendResult(String operation, String msg, boolean isFromERP)
	{
		if (isFromERP && StringUtils.isNullString(msg))
		{
			msg = "ERP:    " + msg;
		}
		if (msg == null)
		{
			msg = "<null>";
		}
		String temp = (operation == null ? "<null>" : operation).concat(":").concat(msg);
		if (this.exportResult.length() != 0)
		{
			this.exportResult.append(";").append(LINE_SEPERATOR).append(temp);
		}
		else
		{
			this.exportResult.append(temp);
		}
		this.stopCount();
	}

	/**
	 * <b>serviceConfig</b>????????????Address??????????????????????????????ip??????????????????????????????
	 * 
	 * @param serviceConfig
	 */
	protected void decorateServieConfig(ERPServiceConfig ERPConfig)
	{
		ERPConfig.setErpServerAddress("http://" + ERPConfig.getERPServerIP() + ":" + ERPConfig.getERPServerPort() + "/" + ERPConfig.getERPServerName());
		if (this.ERPConfig == null)
		{
			this.ERPConfig = ERPConfig;
		}
	}

	/**
	 * ????????????UI??????
	 * 
	 * @param serverDef
	 * @return
	 * @throws Exception
	 */
	public abstract List<ERPMoreCompanies> getMoreCompanyThroughWS(ERPServiceConfig serverDef) throws Exception;

	/**
	 * ?????????????????????ERP
	 * 
	 * @param codeGuidList
	 * @param queuejob
	 * @param userId
	 * @param serviceConfig
	 * @return
	 * @throws ServiceRequestException
	 */
	public BooleanResult export(List<String> codeGuidList, Queue queuejob, String userId, ERPServiceConfig serviceConfig) throws ServiceRequestException
	{
		BooleanResult finalResult = null;
		try
		{
			this.beforeExport(queuejob, userId, serviceConfig);
			long start = System.currentTimeMillis();
			this.integrate.getAllCFDataWithChildren(codeGuidList);
			long end = System.currentTimeMillis();
			DynaLogger.debug("dyna.app.service.brs.erpi.dataExport.IntegrateERP#generateAllCFData time consumed: " + (end - start) + "ms");
			if (!this.integrate.getDataErrorList().isEmpty())
			{
				return new BooleanResult(false, this.integrate.printDataErrorList());
			}
			this.integrate.fillExportGuidMap();
			start = System.currentTimeMillis();
			final BooleanResult result = this.doExportWork4CF();
			finalResult = new BooleanResult(result.getFlag());
			finalResult.setDetail(this.exportResult.toString() + ERPTransferStub.LINE_SEPERATOR);
			end = System.currentTimeMillis();
			DynaLogger.debug("dyna.app.service.brs.erpi.dataExport.IntegrateERP#exportCF2ERP time consumed: " + (end - start) + "ms");
			return finalResult;
		}
		catch (Exception t)
		{
			DynaLogger.error(t);
			String msgId = null;
			if (t instanceof ServiceRequestException)
			{
				msgId = ((ServiceRequestException) t).getMsrId();
				throw new ServiceRequestException(msgId, this.exportResult.toString(), t, ((ServiceRequestException) t).getArgs());
			}
			else
			{
				throw new ServiceRequestException(msgId, this.exportResult.toString(), t);
			}
		}
		finally
		{
			this.afterExport();
		}
	}

	/**
	 * ?????????????????????ERP
	 * 
	 * @param queuejob
	 * @param userId2
	 * @param serviceConfig
	 * @throws ServiceRequestException
	 */
	private void beforeExport(Queue jobQueue, String userId, ERPServiceConfig serviceConfig) throws ServiceRequestException
	{
		this.decorateServieConfig(serviceConfig);
		ERPQuerySchemaParameterObject schemaParameterObj = new ERPQuerySchemaParameterObject();
		schemaParameterObj.setECChangeType(null);
		schemaParameterObj.setERPType(this.ERPType);
		schemaParameterObj.setEC(false);
		schemaParameterObj.setSchemaName(serviceConfig.getSchemaName());
		try
		{
			this.schema = this.stubService.getSchemaByName(schemaParameterObj);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (this.schema == null)
		{
			throw new IllegalArgumentException("schema not found: " + jobQueue.getFieldd());
		}
		if (this.schema.getOperationList().isEmpty())
		{
			throw new IllegalArgumentException("no include attribute for " + this.schema.getName() + " in " + this.integrate.getXMLPath());
		}
		this.userId = userId == null ? "" : userId;
		this.jobId = jobQueue.getFieldh();
		this.job = jobQueue;
		this.factory = jobQueue.getFieldf();
		this.lang = LanguageEnum.getById(jobQueue.getFieldc());
		this.useCross = this.isUseCross(serviceConfig);
		this.exportSuccess = this.stubService.getMSRM().getMSRString("ID_APP_INTEGRATEWF_ERP_DATA_SUCESS", this.lang.toString());
		this.jobGUID = jobQueue.getGuid();
		if (this.schema == null)
		{
			throw new IllegalArgumentException("schema not found: " + jobQueue.getFieldd());
		}
	}

	/**
	 * ??????????????????????????????xml??????(??????XML?????????????????????) <br/>
	 * ???????????????log/ERP
	 * ????????????????????????????????? ???cross???????????????
	 * 
	 * @param content
	 * @throws IOException
	 */
	@Deprecated
	protected void saveTempFile(String content)
	{
		if (!this.integrate.isSaveTempFile())
		{
			return;
		}
		try
		{
			this.saveFile(this.getXMLPackNo() + ".xml", content);
		}
		catch (IOException e)
		{
			DynaLogger.error(e);
		}
	}

	protected void saveTempFile(String fileName, String content)
	{
		if (!this.integrate.isSaveTempFile())
		{
			return;
		}
		try
		{
			this.saveFile(fileName, content);
		}
		catch (IOException e)
		{
			DynaLogger.error(e);
		}
	}

	/**
	 * ????????????????????????
	 * 
	 * @param fileName
	 * @param content
	 * @throws IOException
	 */
	public void saveComparisonFile(String fileName, String content)
	{
		if (!this.integrate.isPrintData4Comparison())
		{
			return;
		}
		try
		{
			this.saveFile(fileName, content);
		}
		catch (IOException e)
		{
			DynaLogger.error(e);
		}
	}

	private void saveFile(String fileName, String content) throws IOException
	{

		File directory = new File("log" + File.separator + "ERP");
		if (!directory.exists())
		{
			directory.mkdir();
		}
		File file = new File(directory, fileName);
		if (!file.exists())
		{
			file.createNewFile();
		}
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
			bw.write(content);
		}
		finally
		{
			// finally?????????????????????????????????????????????try????????????
			if (bw != null)
			{
				try
				{
					bw.close();
				}
				catch (IOException e)
				{
					DynaLogger.error(e);
				}
			}
		}
	}

	/**
	 * get the default operationPollingTime, 20s is default
	 * 
	 * @return
	 */
	public int getDefaultOperationPollingTime()
	{
		return operationPollingTime;
	}

	/**
	 * get the default operationLiveTime, 300s is default
	 * 
	 * @return
	 */
	public int getDefaultOperationLiveTime()
	{
		return operationLiveTime;
	}

	/**
	 * should omit declaration in xml, default is false
	 * 
	 * @return
	 */
	protected boolean isOmitDeclarationInXML()
	{
		return false;
	}

	/**
	 * should expand empty tag in xml, default is false
	 * 
	 * @return
	 */
	protected boolean isExpandEmptyTagInXML()
	{
		return false;
	}

	/**
	 * ????????????ERPStub.paramMap????????????
	 * 
	 * @param name
	 * @return
	 */
	public String getUnusualParameter(String name)
	{
		return ERPStub.paramMap.get(this.ERPType.getProName() + "." + name);
	}

	/**
	 * set the parameter name-value pair
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public void setUnusualParameter(String name, String value)
	{
		ERPStub.paramMap.put(this.ERPType.getProName() + "." + name, value);
	}

	/**
	 * ????????????????????????tempTable
	 * 
	 * @throws ServiceRequestException
	 */
	public void checkTempTable() throws ServiceRequestException
	{
		ERPTempTableInfo info = null;
		String serverIP = null;
		Map<String, Object> map = null;
		List<ERPTempTableInfo> infoList = null;
		StringBuffer buffer = new StringBuffer();
		map = new HashMap<String, Object>();
		map.put("ERPTYPE", this.ERPType.getProName());
		map.put("FACTORY", this.factory);
		if (!useCross)
		{
			serverIP = this.ERPConfig.getERPServerIP();
		}
		else
		{
			serverIP = this.export2Server.getServiceIP();
		}
		map.put("SERVERIP", serverIP);
		stubService = this.getStubService();
		info = stubService.getBaseTableInfo(map);
		if (info == null)
		{
			SyncModelService syncModelService = this.stubService.getSyncModelService();

			int size = 0;
			map = new HashMap<String, Object>();
			map.put("ERPTYPE", this.ERPType.getProName());
			infoList = stubService.listBaseTableInfo(map);
			if (!SetUtils.isNullList(infoList))
			{
				size = infoList.size();
			}
			buffer.append("ERPDATA$_").append(this.ERPType.getProName()).append("_").append(size);
			tempTableName = buffer.toString().toLowerCase();
			map = new HashMap<String, Object>();
			map.put("table", tempTableName);
			map.put("fielda", "itemguid "+syncModelService.getColumnDBType(FieldTypeEnum.STRING, String.valueOf(32)));
			map.put("fieldb", "category "+syncModelService.getColumnDBType(FieldTypeEnum.STRING, String.valueOf(20)));
			map.put("fieldc", "jobguid "+syncModelService.getColumnDBType(FieldTypeEnum.STRING, String.valueOf(32)));
			map.put("fieldd", "stamp "+syncModelService.getColumnDBType(FieldTypeEnum.STRING, String.valueOf(32)));
			map.put("fielde", "isfirst "+syncModelService.getColumnDBType(FieldTypeEnum.STRING, String.valueOf(10)));
						
			map.put("indexname", "ux01_" + tempTableName);
			stubService.createTable(map);
			info = new ERPTempTableInfo();
			info.setERPTYPE(this.ERPType.getProName());
			info.setFACTORY(factory);
			info.setSERVERIP(serverIP);
			info.setBASETABLENAME(tempTableName);
			stubService.insertBaseTableInfo(info);
		}
		else
		{
			tempTableName = info.getBASETABLENAME();
		}
	}

	/**
	 * ?????????cross????????????crossconf????????????
	 */
	public void checkServer()
	{
		if (this.useCross)
		{
			CrossServiceConfig crossServiceConfig = CrossConfigureManager.getInstance().getCrossServiceConfig();
			List<CrossIntegrate> servers = new ArrayList<CrossIntegrate>();
			servers = crossServiceConfig.getServices();
			boolean crossConfigured = false;
			if (!SetUtils.isNullList(servers))
			{
				for (CrossIntegrate crossServer : servers)
				{
					if (this.getERPTypeNameAsList().contains(crossServer.getServiceProd()))
					{
						crossConfigured = true;
						export2Server = crossServer;
						break;
					}
				}
			}
			if (!crossConfigured)
			{
				throw new IllegalArgumentException(msrm.getMSRString("ID_APP_ERPI_CROSS_SYNCHRONIZE", lang.toString()));
			}
		}
	}

	// =======inner class================
	class TimeCounter extends Thread
	{
		private int count = 0;

		@Override
		public void run()
		{
			while (ERPTransferStub.this.timerRunning)
			{
				try
				{
					Thread.sleep(1000);
					this.count++;
					if (this.count >= ERPTransferStub.this.operationLiveTime)
					{
						ERPTransferStub.this.timeOver = true;
						ERPTransferStub.this.timerRunning = false;
						break;
					}
				}
				catch (InterruptedException e)
				{
					DynaLogger.debug("dyna.app.service.brs.erpi.ERPTransferStub.TimeCounter#run() was interrupted");
				}
			}
		}
	}

	public BooleanResult getJobStatusBySeqkeyFromERP(ERPServiceConfig conf, Queue job) throws Exception
	{
		return new BooleanResult(false, "");
	}
}

/**
 * throw TimeoutException when time out
 * 
 * @author chega
 *         2013-5-22
 */
class TimeoutException extends RuntimeException
{
	TimeoutException(String msg)
	{
		super(msg);
	}
}
