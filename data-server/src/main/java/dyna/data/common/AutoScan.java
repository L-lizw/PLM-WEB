package dyna.data.common;

import dyna.common.annotation.Cache;
import dyna.common.annotation.EntryMapper;
import dyna.common.bean.data.DynamicTableBean;
import dyna.common.bean.data.SystemObject;
import dyna.common.util.PackageScanUtil;
import dyna.common.util.SetUtils;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Lizw
 * @date 2022/1/18
 **/
@Data
@Component
public class AutoScan<T extends SystemObject>
{
	private static final String SCAN_BEAN_ANNOTATION_PACKAGE = "dyna.common.dto";

	private Map<Class,Class>                            entryMapperMap;

	private Map<String, DynamicTableBean<T>>            dynamicTableBeanMap;


	public void init()
	{
		this.loadTableData();
		this.loadEntryDaoInfo();
	}


	private void loadEntryDaoInfo()
	{
		entryMapperMap = new HashMap<>();
		Set<Class<?>> set = PackageScanUtil.scanAnnotation("dyna.common", EntryMapper.class);
		if (!SetUtils.isNullSet(set))
		{
			for (Class<?> entryClass : set)
			{
				EntryMapper mapperClass = entryClass.getAnnotation(EntryMapper.class);
				if (mapperClass != null)
				{
					entryMapperMap.put(entryClass, mapperClass.value());
				}
			}
		}
	}

	/**
	 * 取得通过包扫描得到的缓存类信息
	 *
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	private void loadTableData()
	{
		dynamicTableBeanMap = new HashMap<>();
		Set<Class<?>> set = PackageScanUtil.scanAnnotation(SCAN_BEAN_ANNOTATION_PACKAGE, Cache.class);
		if (!SetUtils.isNullSet(set))
		{
			for (Class<?> clz : set)
			{
				Cache cacheAnnotation = clz.getAnnotation(Cache.class);
				if (cacheAnnotation != null)
				{
					DynamicTableBean tableBean = new DynamicTableBean();
					tableBean.setCache(true);
					tableBean.setBeanClass(clz);
					dynamicTableBeanMap.put(clz.getName(), tableBean);
				}

			}
		}
	}

}
