/*
 * Copyright (C) 2005-2016 Christian P. Lerch <christian.p.lerch [at] gmail [dot] com>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution. If not, see <http://www.gnu.org/licenses/>.
 */
package org.kmworks.portal.rpc.server;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ClassLoaderObjectInputStream;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodKey;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.security.ac.AccessControlThreadLocal;
import com.liferay.portal.security.auth.HttpPrincipal;
import com.liferay.portal.security.auth.PrincipalThreadLocal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Christian P. Lerch
 */
public class RPCTunnelServlet extends HttpServlet {


	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws IOException {
    
    ClassLoader classLoader = PortalClassLoaderUtil.getClassLoader();

		ObjectInputStream ois;
		try {
			ois = new ClassLoaderObjectInputStream(request.getInputStream(), classLoader);
		} catch (IOException ioe) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(ioe, ioe);
			}
			return;
		}

		Object returnObj = null;
		boolean remoteAccess = AccessControlThreadLocal.isRemoteAccess();

		try {
			AccessControlThreadLocal.setRemoteAccess(true);

			Object obj = ois.readObject();
			
			@SuppressWarnings("unchecked")
			ObjectValuePair<HttpPrincipal, MethodHandler> ovp = (ObjectValuePair<HttpPrincipal, MethodHandler>)obj;

			//HttpPrincipal principal = ovp.getKey();
      MethodHandler methodHandler = ovp.getValue();

			if (methodHandler != null) {
				MethodKey methodKey = methodHandler.getMethodKey();

				if (!isValidRequest(methodKey.getDeclaringClass())) {
					return;
				}

        if (isServiceRequest(methodKey.getDeclaringClass())) {
          // Simulate login
          PrincipalThreadLocal.setName(ovp.getKey().getLogin());
          PrincipalThreadLocal.setPassword(ovp.getKey().getPassword());
        }

        returnObj = methodHandler.invoke(true);
			}
		} catch (InvocationTargetException ite) {
			returnObj = ite.getCause();

			if (!(returnObj instanceof PortalException)) {
				LOG.error(ite, ite);

				if (returnObj != null) {
					Throwable throwable = (Throwable)returnObj;

					returnObj = new SystemException(throwable.getMessage());
				} else {
					returnObj = new SystemException();
				}
			}
		} catch (Exception e) {
			LOG.error(e, e);
		} finally {
			AccessControlThreadLocal.setRemoteAccess(remoteAccess);
		}

		if (returnObj != null) {
			try {
				returnObj = makeSerializable(returnObj);
		        try (ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream())) {
		          oos.writeObject(returnObj);
		          oos.flush();
		        }
			} catch (Exception ioe) {
				LOG.error(ioe, ioe);
				throw ioe;
			}
		}
	}

	protected boolean isValidRequest(Class<?> clazz) {
		final String className = clazz.getName();
		return className.contains(".service.") && className.endsWith("ServiceUtil"); 
	}
  
  protected boolean isServiceRequest(Class<?> clazz) {
    final String className = clazz.getName();
    return className.contains(".service.") 
            && className.endsWith("ServiceUtil")
            && !className.endsWith("LocalServiceUtil");
  }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object makeSerializable(Object obj) {
		if (obj instanceof Serializable) {
			return obj;
		}
    if (obj instanceof java.io.InputStream) {
      // Save (InputStream)obj contents to local temp file cache on the server -> cacheId
      // return new SerializableDownloadStream(cacheId);
      // the saved temp file can then be downloaded by the client through the RPC Download InputStream Servlet 
      // using an URL with the serialized cacheId
    }
		if (obj.getClass().getName().equals("java.util.ArrayList$SubList")) {
			return new java.util.ArrayList((java.util.List)obj);
		}
		throw new RuntimeException("Unhandled not serializable type detected: " + obj.getClass().getName());
	}

  private static final Class<?> SELF = RPCTunnelServlet.class;
	private static final Log LOG = LogFactoryUtil.getLog(SELF);
}