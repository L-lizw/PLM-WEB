package dyna.app.core.track;

import dyna.app.server.context.ApplicationServerContext;
import dyna.common.bean.track.TrackerBuilder;
import dyna.net.service.brs.Async;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * @author Lizw
 * @date 2022/2/7
 **/
@Aspect
@Configuration
public class TrackAspectImpl
{
	@Autowired
	private ApplicationServerContext serverContext;

	//todo
//	@Autowired
//	private Async   async;

	@Pointcut("@annotation(dyna.app.core.track.annotation.Tracked)")
	public void track()
	{

	}

	@Around("track()")
	public void doTrack(ProceedingJoinPoint proceedingJoinPoint)
	{
		String method = proceedingJoinPoint.getSignature().getDeclaringTypeName() + "." + proceedingJoinPoint.getSignature().getName();
		Class clazz = proceedingJoinPoint.getSignature().getDeclaringType();
		try
		{
			//todo   track
			Object retObject = proceedingJoinPoint.proceed();

			Method method1 = null;

			clazz.getMethod(proceedingJoinPoint.getSignature().getName(), null);
			TrackerBuilder trackerBuilder = this.serverContext.getTrackerManager().getTrackerBuilder(method1);
			if (trackerBuilder != null)
			{
//				async.systemTrack(trackerBuilder, null, method1, proceedingJoinPoint.getArgs(), retObject);
			}

		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace();
		}
//		catch (NoSuchMethodException e)
//		{
//			e.printStackTrace();
//		}

	}

}
