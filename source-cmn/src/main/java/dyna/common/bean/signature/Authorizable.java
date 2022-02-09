/*
 * Copyright (C) DCIS 版权所有
 * 功能描述: Authorizable接口
 * 提供系统接入认证接口
 * Wanglei 2010-3-24
 */
package dyna.common.bean.signature;

import dyna.common.exception.AuthorizeException;

import java.lang.reflect.Method;

/**
 * 提供系统接入身份认证接口
 * 
 * @author Wanglei
 * 
 */
public interface Authorizable
{

	public void authorize(Method method, Object... args) throws AuthorizeException;
}
