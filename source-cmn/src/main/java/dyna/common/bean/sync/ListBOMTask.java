package dyna.common.bean.sync;

import dyna.common.bean.data.structure.BOMStructure;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lizw
 * @date 2022/1/30
 **/
@Data
@NoArgsConstructor
public class ListBOMTask implements Serializable
{
	private int level;

	private String end1guid;

	private List<BOMStructure> bomList;

}
