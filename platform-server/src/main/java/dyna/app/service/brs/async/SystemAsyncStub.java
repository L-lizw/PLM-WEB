package dyna.app.service.brs.async;

import dyna.app.service.AbstractServiceStub;
import dyna.common.bean.signature.Signature;
import dyna.common.bean.track.Tracker;
import dyna.common.bean.track.TrackerBuilder;
import dyna.common.dto.SysTrack;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.LanguageEnum;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author Lizw
 * @date 2022/1/27
 **/
@Component
public class SystemAsyncStub extends AbstractServiceStub<AsyncImpl>
{
	 protected void  systemTrack(TrackerBuilder builder, Signature signature, Method method, Object[] parameters, Object result, LanguageEnum languageEnum)
	 {
		 Tracker tracker = builder.make(signature, method, result, parameters, languageEnum);
		 if (tracker == null)
		 {
			 return;
		 }
		 try
		 {
			 SysTrack sysTrack = tracker.persist();
			 this.stubService.getSystemDataService().save(sysTrack);
			 // 将日志写入远程服务器
			 tracker.getSyslogString();
		 }
		 catch (Exception e)
		 {
			 DynaLogger.error("[" + method.getName() + "] tracker persistence error: " + e);
		 }

	 }

}
