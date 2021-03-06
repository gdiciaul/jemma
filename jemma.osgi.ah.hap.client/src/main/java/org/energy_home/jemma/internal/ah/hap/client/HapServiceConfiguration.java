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
package org.energy_home.jemma.internal.ah.hap.client;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.ah.hap.client.EHContainers;

public class HapServiceConfiguration {
	private static final Log log = LogFactory.getLog(HapServiceConfiguration.class);
	
	private static final String OSGI_INSTANCE_AREA = "osgi.instance.area";
	private static final String OSGI_CONFIGURATION_AREA = "osgi.configuration.area";
	private static final String HAP_CONFIG_FILENAME = "org.energy_home.jemma.ah.hap.client.properties";
	
	public static String BATCH_REQUESTS_DIR_NAME_PREFIX = "org.energy_home.jemma.ah.hap.client.";

	private static final String BATCH_REQUEST_BASE_DIR_PROPERY_NAME = "org.energy_home.jemma.ah.hap.client.cacheFilesDir";
	
	private static final String BATCH_REQUEST_TIMEOUT_PROPERY_NAME = "it.telecomitalia.ah.hap.client.batchMillisecTimeout";
	private static final String SEND_EMPTY_BATCH_REQUEST_PROPERY_NAME = "it.telecomitalia.ah.hap.client.sendEmptyBatchRequest";
	private static final String CACHED_ATTRIBUTE_ID_FILTER_PROPERY_NAME = "it.telecomitalia.ah.hap.client.cachedAttributeIdFilter";
	private static final String LOCAL_ONLY_ATTRIBUTE_ID_FILTER_PROPERY_NAME = "it.telecomitalia.ah.hap.client.localOnlyAttributeIdFilter";

	private static File f = null;
	private static Properties configProperties = null;
	
	public static boolean USE_PERSISTENT_BUFFER = false;
	public static int BATCH_REQUEST_TIMEOUT = 300000;
	public static boolean SEND_EMPTY_BATCH_REQUEST = true;
	public static String[] CACHED_ATTRIBUTE_ID_FILTER = null;
	public static String[] LOCAL_ONLY_ATTRIBUTE_ID_FILTER = null;

	private static String[] propertyValueToStringArray(String propertyValue) {
		propertyValue = propertyValue.replace("[", "");
		propertyValue = propertyValue.replace("]", "");
		propertyValue = propertyValue.trim();
		StringTokenizer st = new StringTokenizer(propertyValue, ",");
		String[] result = new String[st.countTokens()];
		int i=0;
		while (st.hasMoreElements()) {
			result[i] = ((String) st.nextElement()).trim();
			i++;
		}
		return result;
	}
	
	public static synchronized void init() {
		loadProperties();
		String cacheDirName = configProperties.getProperty(BATCH_REQUEST_BASE_DIR_PROPERY_NAME);
		if (Utils.isNullOrEmpty(cacheDirName))
			cacheDirName = System.getProperty(BATCH_REQUEST_BASE_DIR_PROPERY_NAME);
		if (!Utils.isNullOrEmpty(cacheDirName)) {
			USE_PERSISTENT_BUFFER = true;
			BATCH_REQUESTS_DIR_NAME_PREFIX = cacheDirName + "/" + BATCH_REQUESTS_DIR_NAME_PREFIX;
		}
		log.info(String.format("BATCH_REQUESTS_DIR_NAME_PREFIX: %s", BATCH_REQUESTS_DIR_NAME_PREFIX));

		String cachedAttributeIdFilterStr = configProperties.getProperty(CACHED_ATTRIBUTE_ID_FILTER_PROPERY_NAME);
		if (Utils.isNullOrEmpty(cachedAttributeIdFilterStr))		
			cachedAttributeIdFilterStr = System.getProperty(CACHED_ATTRIBUTE_ID_FILTER_PROPERY_NAME);
		if (!Utils.isNullOrEmpty(cachedAttributeIdFilterStr)) {
			// TODO!!! this is a hack for compatibility with old system property configured in Energy@home trial
			if (cachedAttributeIdFilterStr.equals("ah.eh.esp.energySum"))
				cachedAttributeIdFilterStr = EHContainers.attrId_ah_eh_esp_deliveredEnergySum;
			CACHED_ATTRIBUTE_ID_FILTER = propertyValueToStringArray(cachedAttributeIdFilterStr);
		}	
		log.info(String.format("CACHED_ATTRIBUTE_ID_FILTER: %s", cachedAttributeIdFilterStr));
		
		String localOnlyAttributeIdFilterStr = configProperties.getProperty(LOCAL_ONLY_ATTRIBUTE_ID_FILTER_PROPERY_NAME);
		if (Utils.isNullOrEmpty(localOnlyAttributeIdFilterStr))		
			localOnlyAttributeIdFilterStr = System.getProperty(LOCAL_ONLY_ATTRIBUTE_ID_FILTER_PROPERY_NAME);
		if (Utils.isNullOrEmpty(localOnlyAttributeIdFilterStr))
			// TODO!!! this is a hack for compatibility with default configuration in Energy@home trial
			localOnlyAttributeIdFilterStr = EHContainers.attrId_ah_eh_esp_deliveredPower+","+EHContainers.attrId_ah_eh_esp_onOffStatus;
		if (!Utils.isNullOrEmpty(localOnlyAttributeIdFilterStr)) {
			LOCAL_ONLY_ATTRIBUTE_ID_FILTER = propertyValueToStringArray(localOnlyAttributeIdFilterStr);
		}	
		log.info(String.format("LOCAL_ONLY_ATTRIBUTE_ID_FILTER: %s", localOnlyAttributeIdFilterStr));

		String batchRequestTimeout = configProperties.getProperty(BATCH_REQUEST_TIMEOUT_PROPERY_NAME);
		if (Utils.isNullOrEmpty(batchRequestTimeout))
			batchRequestTimeout = System.getProperty(BATCH_REQUEST_TIMEOUT_PROPERY_NAME);
		if (!Utils.isNullOrEmpty(batchRequestTimeout)) {
			try {
				BATCH_REQUEST_TIMEOUT = Integer.parseInt(batchRequestTimeout); 				
			} catch (Exception e) {
				log.error("Error while parsing batch request timeout", e);
			}
		}
		log.info(String.format("BATCH_REQUEST_TIMEOUT: %s", BATCH_REQUEST_TIMEOUT));
			
		String sendEmptyBatchRequest = configProperties.getProperty(SEND_EMPTY_BATCH_REQUEST_PROPERY_NAME);
		if (!Utils.isNullOrEmpty(sendEmptyBatchRequest))
			sendEmptyBatchRequest = System.getProperty(SEND_EMPTY_BATCH_REQUEST_PROPERY_NAME);
		if (!Utils.isNullOrEmpty(sendEmptyBatchRequest)) {
			try {
				SEND_EMPTY_BATCH_REQUEST = Boolean.parseBoolean(sendEmptyBatchRequest); 				
			} catch (Exception e) {
				log.error("Error while parsing send empty batch request property", e);
			}
		}
		log.info(String.format("SEND_EMPTY_BATCH_REQUEST: %s", SEND_EMPTY_BATCH_REQUEST));
	}
	
	public static synchronized void loadProperties() {
		String instanceArea = null;
		URL url = null;

		instanceArea = System.getProperty(OSGI_INSTANCE_AREA);
		if (instanceArea == null) {
			instanceArea = System.getProperty(OSGI_CONFIGURATION_AREA);
			if (instanceArea == null) {
				log.error("Unable to get an area where to store preferences");
				return;
			}
		}
		try {
			url = new URL(instanceArea + HAP_CONFIG_FILENAME);
			configProperties = new Properties();
			f = new File(url.getFile());
			if (f.exists()) {
				configProperties.load(new FileInputStream(f));
			}
		} catch (Exception e) {
			log.error("Problems while loading HAP configuration parameters", e);
		}		
	}
}
