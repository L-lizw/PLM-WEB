/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: SessionActiveTime
 * Wanglei 2012-12-20
 */
package dyna.common.annotation;

import java.lang.annotation.*;

/**
 * @author qiuxq
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SessionActiveTime
{
	boolean isUpdate() default true;
}
