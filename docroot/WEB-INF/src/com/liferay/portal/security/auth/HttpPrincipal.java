/*
 * Copyright (c) 2005-2016 Christian P. Lerch <christian.p.lerch [at] gmail [dot] com>
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
package com.liferay.portal.security.auth;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

///- import com.liferay.portal.PwdEncryptorException;
///- import com.liferay.portal.security.pwd.PasswordEncryptorUtil;

import java.io.Serializable;

/**
 * A simplified version of com.liferay.portal.security.auth.HttpPrincipal.
 * It does without digested password (i.e. allows only plain-text passwords),
 * because this would pull in a huge number of dependencies not used anywhere else.
 * 
 * @author Brian Wing Shun Chan (original author)
 * @author Christian P. Lerch (remove dependencies on PasswordEncryptorUtil)
 */
public class HttpPrincipal implements Serializable {

	public HttpPrincipal() {
	}

	public HttpPrincipal(String url) {
		_url = url;
	}

	public HttpPrincipal(String url, String login, String password) {
		this(url, login, password, false);
	}

	public HttpPrincipal(
		String url, String login, String password, boolean digested) {

		_url = url;
		_login = login;
		_password = password;
/*/-
		if (digested) {
			_password = password;
		}
		else {
			try {
				_password = PasswordEncryptorUtil.encrypt(password);
			}
			catch (PwdEncryptorException pee) {
				_log.error(pee, pee);
			}
		}
/*/
	}

	public long getCompanyId() {
		return _companyId;
	}

	public String getLogin() {
		return _login;
	}

	public String getPassword() {
		return _password;
	}

	public String getUrl() {
		return _url;
	}

	public void setCompanyId(long companyId) {
		_companyId = companyId;
	}

	public void setPassword(String password) {
		_password = password;
	}

	private static Log _log = LogFactoryUtil.getLog(HttpPrincipal.class);

	private long _companyId;
	private String _login;
	private String _password;
	private String _url;

}