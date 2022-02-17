package dyna.common.util;

import org.apache.poi.hssf.record.formula.functions.T;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PackageScanUtil
{
	public static Set<Class<?>> scanAnnotation(String packageName, Class<? extends Annotation> annotationTag)
	{
		Set<Class<?>> classSet = null;
		try
		{
			classSet = ClassUtils.listAllClassOfPackage(packageName);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		Set<Class<?>> set = new HashSet<Class<?>>();
		if (!SetUtils.isNullSet(classSet))
		{
			for (Iterator<Class<?>> it = classSet.iterator(); it.hasNext();)
			{
				Class<?> clz = it.next();
				if (clz.isAnnotationPresent(annotationTag))
				{
					set.add(clz);
				}
			}
		}
		return set;
	}

	public static Set<Class<?>>  findImplementationByInterface(String packageName, Class<?> clazz)
	{
		Set<Class<?>> classSet = null;
		try
		{
			classSet = ClassUtils.listAllClassOfPackage(packageName);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		Set<Class<?>> set = new HashSet<>();
		if (!SetUtils.isNullSet(classSet))
		{
			for (Iterator<Class<?>> it = classSet.iterator(); it.hasNext();)
			{
				Class<?> clz = it.next();
				if (clazz.isAssignableFrom(clz))
				{
					set.add(clz);
				}
			}
		}
		return set;
	}
}
