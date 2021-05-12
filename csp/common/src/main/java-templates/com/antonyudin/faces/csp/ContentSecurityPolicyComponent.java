
package com.antonyudin.faces.csp;


import ${jee.faces}.component.FacesComponent;
import ${jee.faces}.component.UINamingContainer;

import ${jee.inject}.Inject;


@FacesComponent("com.antonyudin.faces.csp.ContentSecurityPolicyComponent")
public class ContentSecurityPolicyComponent extends UINamingContainer implements java.io.Serializable {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		ContentSecurityPolicyComponent.class.getName()
	);


	@Override
	public String getFamily() {
		return "${jee.faces}.NamingContainer";
	}


	private boolean inline = false;

	public boolean isInline() {
		return inline;
	}

	public void setInline(final boolean value) {
		logger.fine(() -> "setInline(" + inline + " -> " + value + ")");
		inline = value;
	}


	private boolean unsafeInline = false;

	public boolean isUnsafeInline() {
		return unsafeInline;
	}

	public void setUnsafeInline(final boolean value) {
		logger.fine(() -> "setUnsafeInline(" + unsafeInline + " -> " + value + ")");
		unsafeInline = value;
	}



	@Override
	public void setInView(final boolean value) {
		if (isRendered()) {
			logger.fine(() -> "setInView(" + value + "): " + this);
			logger.fine(() -> "\trendered: [" + isRendered() + "]");
			logger.fine(() -> "\tinline: [" + isInline() + "]");
			logger.fine(() -> "\tattributes.keySet: [" + getAttributes().keySet() + "]");
			logger.fine(() -> "\tattributes.policy: " + getAttributes().get("policy"));
			ContentSecurityPolicy.class.cast(getAttributes().get("policy")).enable(
				isInline(),
				isUnsafeInline()
			);
		}
	}

}

