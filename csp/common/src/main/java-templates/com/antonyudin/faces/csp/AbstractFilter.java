
package com.antonyudin.faces.csp;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import ${jee.inject}.Inject;

import ${jee.servlet}.http.HttpFilter;
import ${jee.servlet}.http.HttpServletRequest;

import ${jee.servlet}.ServletException;
import ${jee.servlet}.FilterConfig;


public class AbstractFilter extends HttpFilter {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		AbstractFilter.class.getName()
	);


	protected ContentSecurityPolicyGenerator generator;


	protected boolean getBoolean(final String name) {
		final var value = getFilterConfig().getInitParameter(name);
		return ((value != null) && value.equalsIgnoreCase("true"));
	}

	@Override
	public void init() throws ServletException {
		try {
			final List<String> contentToHash = new ArrayList<>();

			for (var i = 0;;i++) {
				final var name = "contentToHash." + i;
				final var value = getFilterConfig().getInitParameter(name);
				logger.info("checking [" + name + "] -> [" + value + "]");
				if (value == null)
					break;
				if (value.length() > 2)
					contentToHash.add(value.substring(1, value.length() - 1));
			}

			generator = DefaultContentSecurityPolicyGenerator.newInstance(
				getBoolean("enabled"),
				getBoolean("reportOnly"),
				getFilterConfig().getInitParameter("template"),
				contentToHash
			);
			logger.fine(() -> "generator initialized to [" + generator + "]");
		} catch (java.lang.Exception exception) {
			throw new ServletException(exception);
		}
	}


	@Inject
	protected ContentSecurityPolicy policy;

	protected static Map<String, Object> getEnvironment(final HttpServletRequest request) {

		final Map<String, Object> result = new HashMap<>();

		final var headerNames = request.getHeaderNames();

		while (headerNames.hasMoreElements()) {
			final var name = headerNames.nextElement();
			final var values = request.getHeaders(name);
			if ((name != null) && (values != null))
				result.put(name.toLowerCase(), Collections.list(values));
		}

		return result;
	}

}

