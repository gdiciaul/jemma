/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
 *
 * JEMMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) version 3
 * or later as published by the Free Software Foundation, which accompanies
 * this distribution and is available at http://www.gnu.org/licenses/lgpl.html
 *
 * JEMMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License (LGPL) for more details.
 *
 */
package org.energy_home.jemma.osgi.ah.webui.base;

import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public class WebUIComponent {
	private HttpService httpService = null;
	private final String rootUrl = "/js";

	public synchronized void setHttpService(HttpService s) {
		httpService = s;

		try {
			httpService.registerResources(rootUrl, "web/js", null);
		} catch (NamespaceException e) {
		} catch (Exception e) {
		}
	}

	public synchronized void unsetHttpService(HttpService s) {
		if (httpService == s) {
			try {
				httpService.unregister(rootUrl);
			} catch (Exception e) {
			} finally {
				httpService = null;
			}
		}
	}
}
