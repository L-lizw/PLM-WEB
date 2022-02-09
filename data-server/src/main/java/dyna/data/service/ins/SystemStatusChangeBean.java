package dyna.data.service.ins;

import dyna.common.systemenum.SystemStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @author: duanll
 * @date: 2020年3月26日
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SystemStatusChangeBean
{
	@NonNull
	private SystemStatusEnum					from;

	@NonNull
	private SystemStatusEnum					to;

	private String[]							clearFields;

	private String[]							setValsFields;

	private static List<SystemStatusChangeBean>	statusChangeOptions	= new ArrayList<>();

	static
	{
		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.WIP, SystemStatusEnum.PRE, null, null));
		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.WIP, SystemStatusEnum.RELEASE, null, new String[] { "releasetime" }));
		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.WIP, SystemStatusEnum.ECP, null, null));
		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.WIP, SystemStatusEnum.OBSOLETE, null, new String[] { "obsoletetime", "obsoleteuser" }));

		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.PRE, SystemStatusEnum.WIP, null, null));
		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.PRE, SystemStatusEnum.RELEASE, null, new String[] { "releasetime" }));

		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.RELEASE, SystemStatusEnum.OBSOLETE, null, new String[] { "obsoletetime", "obsoleteuser" }));
		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.RELEASE, SystemStatusEnum.WIP, new String[] { "releasetime" }, null));
		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.RELEASE, SystemStatusEnum.ECP, null, null));

		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.ECP, SystemStatusEnum.RELEASE, null, new String[] { "releasetime" }));
		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.ECP, SystemStatusEnum.WIP, null, null));
		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.ECP, SystemStatusEnum.OBSOLETE, null, new String[] { "obsoletetime", "obsoleteuser" }));

		statusChangeOptions.add(new SystemStatusChangeBean(SystemStatusEnum.OBSOLETE, SystemStatusEnum.RELEASE, new String[] { "obsoletetime", "obsoleteuser" }, null));
	}

	public SystemStatusChangeBean getStatusChangeRule()
	{
		for (SystemStatusChangeBean rule : statusChangeOptions)
		{
			if (rule.getFrom() == this.getFrom() && rule.getTo() == this.getTo())
			{
				return rule;
			}
		}
		return null;
	}
}
