/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

 package org.apache.ranger.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.apache.ranger.view.VXMessage;
import org.apache.ranger.view.VXResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class RESTErrorUtil {

	static final Logger logger = Logger.getLogger(RESTErrorUtil.class);

	@Autowired
	StringUtil stringUtil;

	public static final String TRUE = "true";

	public WebApplicationException createRESTException(VXResponse gjResponse) {
		Response errorResponse = Response
				.status(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST)
				.entity(gjResponse).build();

		WebApplicationException restException = new WebApplicationException(
				errorResponse);
		restException.fillInStackTrace();
		UserSessionBase userSession = ContextUtil.getCurrentUserSession();
		Long sessionId = null;
		String loginId = null;
		if (userSession != null) {
			loginId = userSession.getLoginId();
			sessionId = userSession.getSessionId();
		}

		logger.info("Request failed. SessionId=" + sessionId + ", loginId="
				+ loginId + ", logMessage=" + gjResponse.getMsgDesc(),
				restException);

		return restException;
	}

	public WebApplicationException generateRESTException(VXResponse gjResponse) {
		Response errorResponse = Response
				.status(gjResponse.getStatusCode())
				.entity(gjResponse).build();

		WebApplicationException restException = new WebApplicationException(
				errorResponse);
		restException.fillInStackTrace();
		UserSessionBase userSession = ContextUtil.getCurrentUserSession();
		Long sessionId = null;
		String loginId = null;
		if (userSession != null) {
			loginId = userSession.getLoginId();
			sessionId = userSession.getSessionId();
		}

		logger.info("Request failed. SessionId=" + sessionId + ", loginId="
				+ loginId + ", logMessage=" + gjResponse.getMsgDesc(),
				restException);

		return restException;
	}
	/**
	 * 
	 * @param logMessage
	 *            This is optional
	 * @return
	 */
	public WebApplicationException create403RESTException(String logMessage) {
		Response errorResponse = Response.status(
				javax.servlet.http.HttpServletResponse.SC_FORBIDDEN).build();

		WebApplicationException restException = new WebApplicationException(
				errorResponse);
		restException.fillInStackTrace();
		// TODO:Future:Open: Need to log all these and add user to
		// block list if this is deliberate
		// Get user information
		UserSessionBase userSession = ContextUtil.getCurrentUserSession();
		Long sessionId = null;
		String loginId = null;
		String sessionInfo = "";
		if (userSession != null) {
			loginId = userSession.getLoginId();
			sessionInfo = userSession.toString();
			sessionId = userSession.getSessionId();
		}

		String requestInfo = "";
		try {
			RequestContext reqContext = ContextUtil.getCurrentRequestContext();
			if (reqContext != null) {
				requestInfo = reqContext.toString();
				requestInfo += ", timeTaken="
						+ (System.currentTimeMillis() - reqContext
								.getStartTime());
			}
		} catch (Throwable contextEx) {
			logger.error("Error getting request info", contextEx);
		}

		logger.error("Access restricted. SessionId=" + sessionId + ", loginId="
				+ loginId + ", logMessage=" + logMessage + ", requestInfo="
				+ requestInfo + ", sessionInfo=" + sessionInfo, restException);

		return restException;
	}

	

	
	public Integer parseInt(String value, String errorMessage,
			MessageEnums messageEnum, Long objectId, String fieldName) {
		try {
			if (stringUtil.isEmpty(value)) {
				return null;
			} else {
				return new Integer(value.trim());
			}
		} catch (Throwable t) {
			throw createRESTException(errorMessage, messageEnum, objectId,
					fieldName, value);
		}
	}

	public Integer parseInt(String value, int defaultValue,
			String errorMessage, MessageEnums messageEnum, Long objectId,
			String fieldName) {
		try {
			if (stringUtil.isEmpty(value)) {
				return new Integer(defaultValue);
			} else {
				return new Integer(value.trim());
			}
		} catch (Throwable t) {
			throw createRESTException(errorMessage, messageEnum, objectId,
					fieldName, value);
		}
	}

	public Long parseLong(String value, Long defaultValue) {
		if (stringUtil.isEmpty(value)) {
			return defaultValue;
		}
		return new Long(value.trim());
	}

	public Long parseLong(String value, String errorMessage,
			MessageEnums messageEnum, Long objectId, String fieldName) {
		try {
			if (stringUtil.isEmpty(value)) {
				return null;
			} else {
				return new Long(value.trim());
			}
		} catch (Throwable t) {
			throw createRESTException(errorMessage, messageEnum, objectId,
					fieldName, value);
		}
	}

	

	public String validateString(String value, String regExStr,
			String errorMessage, MessageEnums messageEnum, Long objectId,
			String fieldName) {
		return validateString(value, regExStr, errorMessage, messageEnum,
				objectId, fieldName, false);

	}

	public String validateString(String value, String regExStr,
			String errorMessage, MessageEnums messageEnum, Long objectId,
			String fieldName, boolean isMandatory) {
		if (stringUtil.isEmpty(value)) {
			if (isMandatory) {
				throw createRESTException(errorMessage,
						MessageEnums.NO_INPUT_DATA, objectId, fieldName, null);
			}
			return null;
		}
		value = value.trim();
		if (value.length() != 0) {
			if (!stringUtil.validateString(regExStr, value)) {
				throw createRESTException(errorMessage, messageEnum, objectId,
						fieldName, value);
			}
			return value;
		} else {
			return null;
		}

	}

	public String validateStringForUpdate(String value, String originalValue,
			String regExStr, String errorMessage, MessageEnums messageEnum,
			Long objectId, String fieldName) {
		return validateStringForUpdate(value, originalValue, regExStr,
				errorMessage, messageEnum, objectId, fieldName, false);
	}

	public String validateStringForUpdate(String value, String originalValue,
			String regExStr, String errorMessage, MessageEnums messageEnum,
			Long objectId, String fieldName, boolean isMandatory) {
		if (stringUtil.isEmpty(value)) {
			if (isMandatory) {
				throw createRESTException(errorMessage,
						MessageEnums.NO_INPUT_DATA, objectId, fieldName, null);
			}
			return null;
		}

		if (!value.equalsIgnoreCase(originalValue)) {
			return validateString(value, StringUtil.VALIDATION_NAME,
					errorMessage, messageEnum, objectId, fieldName);
		} else {
			return value;
		}
	}

	public void validateStringList(String value, String[] validValues,
			String errorMessage, Long objectId, String fieldName) {
		for (int i = 0; i < validValues.length; i++) {
			if (validValues[i].equals(value)) {
				return;
			}
		}
		throw createRESTException(errorMessage,
				MessageEnums.INVALID_INPUT_DATA, objectId, fieldName, value);
	}

	

	

	public void validateMinMax(int value, int minValue, int maxValue,
			String errorMessage, Long objectId, String fieldName) {
		if (value < minValue || value > maxValue) {
			throw createRESTException(errorMessage,
					MessageEnums.INPUT_DATA_OUT_OF_BOUND, objectId, fieldName,
					"" + value);
		}
	}


	public WebApplicationException createRESTException(String errorMessage,
			MessageEnums messageEnum, Long objectId, String fieldName,
			String logMessage) {
		List<VXMessage> messageList = new ArrayList<VXMessage>();
		messageList.add(messageEnum.getMessage(objectId, fieldName));

		VXResponse gjResponse = new VXResponse();
		gjResponse.setStatusCode(VXResponse.STATUS_ERROR);
		gjResponse.setMsgDesc(errorMessage);
		gjResponse.setMessageList(messageList);
		WebApplicationException webAppEx = createRESTException(gjResponse);
		logger.info("Validation error:logMessage=" + logMessage + ", response="
				+ gjResponse, webAppEx);
		return webAppEx;
	}

	public WebApplicationException createRESTException(String errorMessage) {
		VXResponse gjResponse = new VXResponse();
		gjResponse.setStatusCode(VXResponse.STATUS_ERROR);
		gjResponse.setMsgDesc(errorMessage);
		WebApplicationException webAppEx = createRESTException(gjResponse);
		logger.info("Operation error. response=" + gjResponse, webAppEx);
		return webAppEx;
	}

	public WebApplicationException createRESTException(String errorMessage,
			MessageEnums messageEnum) {
		List<VXMessage> messageList = new ArrayList<VXMessage>();
		messageList.add(messageEnum.getMessage());

		VXResponse gjResponse = new VXResponse();
		gjResponse.setStatusCode(VXResponse.STATUS_ERROR);
		gjResponse.setMsgDesc(errorMessage);
		gjResponse.setMessageList(messageList);
		WebApplicationException webAppEx = createRESTException(gjResponse);
		logger.info("Operation error. response=" + gjResponse, webAppEx);
		return webAppEx;
	}

	public WebApplicationException createRESTException(int responseCode,
			String logMessage, boolean logError) {
		Response errorResponse = Response
				.status(responseCode).build();

		WebApplicationException restException = new WebApplicationException(
				errorResponse);
		restException.fillInStackTrace();
		UserSessionBase userSession = ContextUtil.getCurrentUserSession();
		Long sessionId = null;
		String loginId = null;
		if (userSession != null) {
			loginId = userSession.getLoginId();
			sessionId = userSession.getSessionId();
		}

		if (logError) {
			logger.info("Request failed. SessionId=" + sessionId + ", loginId="
					+ loginId + ", logMessage=" + logMessage,
					restException);
		}

		return restException;	
	}
	
	
	public Date parseDate(String value, String errorMessage,
			MessageEnums messageEnum, Long objectId, String fieldName,
			String dateFormat) {
		try {
			if (stringUtil.isEmpty(value)) {
				return null;
			} else {
				DateFormat formatter = new SimpleDateFormat(dateFormat);
				return formatter.parse(value);

			}
		} catch (Throwable t) {
			throw createRESTException(errorMessage, messageEnum, objectId,
					fieldName, value);
		}
	}

	public boolean parseBoolean(String value, boolean defaultValue) {
		if (stringUtil.isEmpty(value)) {
			return defaultValue;
		}
		return TRUE.equalsIgnoreCase(value.trim());
	}
	
	public Boolean parseBoolean(String value, String errorMessage,
			MessageEnums messageEnum, Long objectId, String fieldName) {
		try {
			if (stringUtil.isEmpty(value)) {
				return null;
			} else {
				return new Boolean(value.trim());
			}
		} catch (Throwable t) {
			throw createRESTException(errorMessage, messageEnum, objectId,
					fieldName, value);
		}
	}
}
