package dyna.common.conf;

import lombok.Data;

import java.io.Serializable;

@Data
public class SchedulerDefinition  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5513672192222809511L;

	private String		id				= null;

	private int		threadCount				= 1;

	public void setThreadCount(int threadCount) {
		if (threadCount>0)
		{
			this.threadCount = threadCount;
		}
	}

}
