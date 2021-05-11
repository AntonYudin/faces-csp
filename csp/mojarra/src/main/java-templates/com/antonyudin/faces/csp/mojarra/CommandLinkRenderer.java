
package com.antonyudin.faces.csp.mojarra;


import ${jee.inject}.Inject;

import ${jee.faces}.component.UIComponent;

import ${jee.faces}.context.FacesContext;
import ${jee.faces}.context.ResponseWriterWrapper;
import ${jee.faces}.context.FacesContextWrapper;
import ${jee.faces}.context.ResponseWriter;


public class CommandLinkRenderer extends com.sun.faces.renderkit.html_basic.CommandLinkRenderer {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		CommandLinkRenderer.class.getName()
	);


	@Inject
	private com.antonyudin.faces.csp.ContentSecurityPolicy contentSecurityPolicy;


	@Override
	protected void renderAsActive(
		final FacesContext context,
		final UIComponent command
	) throws java.io.IOException {

		logger.fine(() -> "renderAsActive(): " + contentSecurityPolicy);

		final var responseWriter = new ResponseWriterWrapper(context.getResponseWriter()) {
			public void writeAttribute(
				final String name,
				final Object value,
				final String property
			) throws java.io.IOException {
				logger.fine(() -> "writeAttribute(" + name + ", " + value + ", " + property + ")");
				super.writeAttribute(name, value, property);
				if ((name != null) && name.startsWith("on") && (value != null))
					contentSecurityPolicy.addInline(value.toString());
			}

		};

		final var wrapped = new FacesContextWrapper(context) {
			@Override
			public ResponseWriter getResponseWriter() {
				return responseWriter;
			}
		};

		super.renderAsActive(wrapped, command);
	}

}

