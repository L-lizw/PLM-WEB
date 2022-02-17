package dyna.app.startinit;

import dyna.app.conf.yml.ConfigurableServiceImpl;
import dyna.app.util.SpringUtil;
import dyna.common.conf.ServiceDefinition;
import dyna.common.log.DynaLogger;
import dyna.common.util.PackageScanUtil;
import dyna.common.util.SetUtils;
import dyna.net.service.ApplicationService;
import org.apache.poi.hssf.record.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Lizw
 * @date 2022/2/8
 **/

@Order(2)
@Component
public class ApplicationServiceCommonRunner implements CommandLineRunner
{
	private static final String SCAN_BEAN_ANNOTATION_PACKAGE = "dyna.app.service";

	@Autowired
	private ConfigurableServiceImpl configurableService;

	@Override public void run(String... args) throws Exception
	{
		Set<Class<?>> set = PackageScanUtil.findImplementationByInterface(SCAN_BEAN_ANNOTATION_PACKAGE, ApplicationService.class);

		if(!SetUtils.isNullSet(set))
		{
			Map<Integer, ApplicationService> orderServiceMap = new HashMap<>();

			for(Class<?> serviceClass:set)
			{
				ApplicationService service = (ApplicationService) SpringUtil.getBean(serviceClass);
				Order order = serviceClass.getAnnotation(Order.class);
				if(order!=null)
				{
					int sequence = order.value();
					orderServiceMap.put(sequence, service);
					continue;
				}
				System.out.println(serviceClass.getSimpleName());
				ServiceDefinition serviceDefinition = configurableService.getServiceDefinitionByclass(serviceClass.getSimpleName());
				DynaLogger.info(serviceDefinition.getDescription()+"init ...");
				service.init(serviceDefinition);
				DynaLogger.info(serviceDefinition.getDescription()+"init success");
			}

			if(!SetUtils.isNullMap(orderServiceMap))
			{
				List<Integer> orderList = new ArrayList<>(orderServiceMap.keySet());
				Collections.sort(orderList, new Comparator<Integer>()
				{
					@Override public int compare(Integer o1, Integer o2)
					{
						return o1-o2;
					}
				});

				for(Integer order:orderList)
				{
					ApplicationService service = orderServiceMap.get(order);
					ServiceDefinition serviceDefinition = configurableService.getServiceDefinitionByclass(service.getClass().getSimpleName());
					DynaLogger.info(serviceDefinition.getDescription()+"init ...");
					service.init(serviceDefinition);
					DynaLogger.info(serviceDefinition.getDescription()+"init success");
				}
			}
		}
	}
}
