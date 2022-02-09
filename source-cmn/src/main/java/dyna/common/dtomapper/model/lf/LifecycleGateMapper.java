/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: LifecycleGate
 * Jiagang 2010-9-28
 */
package dyna.common.dtomapper.model.lf;

import dyna.common.dto.model.lf.LifecycleGate;
import dyna.common.dtomapper.DynaCacheMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
*
* @author   Lizw
* @date     2021/7/11 16:57
**/
@Mapper
public interface LifecycleGateMapper extends DynaCacheMapper<LifecycleGate>
{
	List<LifecycleGate> selectForLoad();
}
