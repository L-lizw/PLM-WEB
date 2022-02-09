/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: ServiceContextImpl
 * Wanglei 2010-3-30
 */
package dyna.app.server.context;

import dyna.app.core.track.annotation.Tracked;
import dyna.app.core.track.impl.DefaultTrackerBuilderImpl;
import dyna.common.bean.track.TrackerBuilder;

import java.lang.reflect.Method;
import java.util.Observable;

/**
 * 服务的上下文实现
 * 
 * @author Wanglei
 * 
 */
@org.springframework.stereotype.Service
public class ServiceContextImpl
{
	/**
	 * 
	 */
	private static final long					serialVersionUID		= 1037614906399936105L;

	private ServiceContextObservable			scObservable			= new ServiceContextObservable();

	private void buildTrackedMethod(Class<?> interfaceClass, Class<?> implClass)
	{

		Method[] methods = implClass.getMethods();
		if (methods == null || methods.length == 0)
		{
			return;
		}
		Tracked tracked = null;
		Method method = null;
		for (Method m : methods)
		{
			if (!m.isAnnotationPresent(Tracked.class))
			{
				continue;
			}
			tracked = m.getAnnotation(Tracked.class);

			try
			{
				method = interfaceClass.getMethod(m.getName(), m.getParameterTypes());
			}
			catch (Exception e)
			{
				continue;
			}

			TrackerBuilder builder = new DefaultTrackerBuilderImpl();
			builder.setTrackerRendererClass(tracked.renderer(), tracked.description());
			builder.setPersistenceClass(tracked.persistence());
			//TODO
//			this.serverContext.getTrackerManager().bindTrackerBuilder(method, builder);
		}
	}


	public Observable getServiceContextObservable()
	{
		return this.scObservable;
	}

	class ServiceContextObservable extends Observable
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Observable#setChanged()
		 */
		@Override
		protected synchronized void setChanged()
		{
			super.setChanged();
		}

	}
}
