
package com.antonyudin.faces.csp;


import ${jee.inject}.Inject;

import ${jee.servlet}.http.HttpFilter;

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
		generator = DefaultContentSecurityPolicyGenerator.newInstance(
			getBoolean("enabled"),
			getBoolean("reportOnly"),
			getFilterConfig().getInitParameter("template")
		);
		logger.fine(() -> "generator initialized to [" + generator + "]");
	}


	@Inject
	protected ContentSecurityPolicy policy;

}

