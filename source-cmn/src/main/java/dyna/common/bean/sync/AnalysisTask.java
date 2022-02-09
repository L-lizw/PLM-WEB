package dyna.common.bean.sync;

import dyna.common.bean.data.FoundationObject;
import dyna.common.bean.data.ObjectGuid;
import dyna.common.dto.template.wft.WorkflowTemplateScopeRTInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Lizw
 * @date 2022/2/6
 **/
@Data
public class AnalysisTask
{
	private String								credential		= null;

	private ObjectGuid mainObjectGuid = null;

	private List<FoundationObject> bomEnd2List = new LinkedList<>();

	private List<FoundationObject>				assoEnd2List	= new LinkedList<>();

}
