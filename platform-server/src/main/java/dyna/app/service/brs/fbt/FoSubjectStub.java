/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: FoSubjectStub
 * wangweixia 2012-9-7
 */
package dyna.app.service.brs.fbt;

import dyna.app.service.AbstractServiceStub;
import dyna.common.dto.FileOpenSubject;
import dyna.common.exception.DynaDataException;
import dyna.common.exception.ServiceRequestException;
import dyna.common.util.SetUtils;
import dyna.dbcommon.filter.FieldValueEqualsFilter;
import dyna.net.service.data.SystemDataService;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 对应FileOpenSubject的相关操作
 * 
 * @author wangweixia
 * 
 */
@Component
public class FoSubjectStub extends AbstractServiceStub<FBTSImpl>
{

	/**
	 * @param subjectGuid
	 */
	protected void deleteFileOpenSubject(String subjectGuid) throws ServiceRequestException
	{

		SystemDataService sds = this.stubService.getSystemDataService();

		try
		{
			sds.delete(FileOpenSubject.class, subjectGuid);
		}
		catch (DynaDataException e)
		{
			throw ServiceRequestException.createByDynaDataException(e);
		}

	}

	/**
	 * @return
	 */
	protected List<FileOpenSubject> listRootFileOpenSubject() throws ServiceRequestException
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		try
		{
			List<FileOpenSubject> fileSubject = sds.listFromCache(FileOpenSubject.class, new FieldValueEqualsFilter<FileOpenSubject>("PARENTGUID", null));
			return fileSubject;
		}
		catch (DynaDataException e)
		{
			throw ServiceRequestException.createByDynaDataException(e);
		}
	}

	/**
	 * @return
	 */
	protected List<FileOpenSubject> listALLNodeFileOpenSubject() throws ServiceRequestException
	{
		SystemDataService sds = this.stubService.getSystemDataService();
		try
		{
			List<FileOpenSubject> fileSubject = sds.listFromCache(FileOpenSubject.class, null);
			return fileSubject;
		}
		catch (DynaDataException e)
		{
			e.printStackTrace();
			throw ServiceRequestException.createByDynaDataException(e);
		}
	}

	/**
	 * @param fileOpenSubjectGuid
	 * @return
	 */
	protected List<FileOpenSubject> listSubFileOpenSubject(String fileOpenSubjectGuid) throws ServiceRequestException
	{
		SystemDataService sds = this.stubService.getSystemDataService();

		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("GUID", fileOpenSubjectGuid);

		try
		{
			List<FileOpenSubject> arrayList = sds.listFromCache(FileOpenSubject.class, null);
			return shortFileOpenSubjectList(arrayList, fileOpenSubjectGuid);
		}
		catch (DynaDataException e)
		{
			throw ServiceRequestException.createByDynaDataException(e);
		}
	}

	private List<FileOpenSubject> shortFileOpenSubjectList(List<FileOpenSubject> values, String guid)
	{
		if (SetUtils.isNullList(values))
		{
			return null;
		}
		List<FileOpenSubject> results = new ArrayList<FileOpenSubject>();
		for (FileOpenSubject fileOpenSubject : values)
		{
			if (guid.equalsIgnoreCase(fileOpenSubject.getParentGuid()))
			{
				results.add(fileOpenSubject);
			}
		}
		Collections.sort(results, Comparator.comparing(FileOpenSubject::getPosition));
		return results;
	}
}
