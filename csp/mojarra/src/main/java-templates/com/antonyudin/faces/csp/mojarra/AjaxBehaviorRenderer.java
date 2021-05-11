
package com.antonyudin.faces.csp.mojarra;


import ${jee.inject}.Inject;

import ${jee.faces}.component.behavior.ClientBehaviorContext;
import ${jee.faces}.component.behavior.ClientBehavior;


//public class AjaxBehaviorRenderer extends javax.faces.component.behavior.AjaxBehavior {
public class AjaxBehaviorRenderer extends com.sun.faces.renderkit.html_basic.AjaxBehaviorRenderer {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		AjaxBehaviorRenderer.class.getName()
	);


	@Inject
	private com.antonyudin.faces.csp.ContentSecurityPolicy contentSecurityPolicy;


	@Override
	public String getScript(
		final ClientBehaviorContext context,
		final ClientBehavior behavior
	) {
//	public String getScript(final javax.faces.component.behavior.ClientBehaviorContext context) {
		final var result = super.getScript(context, behavior);
		logger.fine(() -> "getScript() result: [" + result + "]");
		logger.fine(() -> "contentSecurityPolicy: [" + contentSecurityPolicy + "]");
		if (result != null)
			contentSecurityPolicy.addInline(result);
		return result;
	}

}

