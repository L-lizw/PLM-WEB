package dyna.dbcommon.filter;

import dyna.common.bean.data.SystemObject;

import java.io.Serializable;

public interface BeanFilter<E extends SystemObject> extends Serializable, Cloneable
{
	boolean match(E o);

	BeanFilter<E> clone();
}
