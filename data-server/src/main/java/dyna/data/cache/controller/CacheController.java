package dyna.data.cache.controller;

import java.util.Map;
import java.util.List;

import dyna.common.bean.data.SystemObject;
import dyna.data.cache.DynaCacheModel;
import dyna.dbcommon.filter.BeanFilter;

public interface CacheController<T extends SystemObject>
{
	public void flush(DynaCacheModel<T> cacheModel);

	// public T fresh(DynaCacheModel<T> cacheModel, T object);

	public void cacheAll(DynaCacheModel<T> cacheModel, Map<String, T> dataMap);

	public T getObject(DynaCacheModel<T> cacheModel, String key);

	public List<T> listObject(DynaCacheModel<T> cacheModel, BeanFilter<T> beanFilter);

	public T removeObject(DynaCacheModel<T> cacheModel, String key);

	public T putObject(DynaCacheModel<T> cacheModel, T object);
}
