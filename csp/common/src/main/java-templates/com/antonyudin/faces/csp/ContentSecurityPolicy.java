
package com.antonyudin.faces.csp;


import java.util.Base64;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import ${jee.inject}.Named;

import ${jee.enterprise}.context.RequestScoped;


@Named("contentSecurityPolicy")
@RequestScoped
public class ContentSecurityPolicy implements java.io.Serializable {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		ContentSecurityPolicy.class.getName()
	);


	private final static Random random = new Random();


	private boolean inlineScripts = false;

	public boolean isInlineScripts() {
		return inlineScripts;
	}


	private boolean unsafeInline = false;

	public boolean isUnsafeInline() {
		return unsafeInline;
	}


	private String currentNonce = null;

	public String getNonce() {

		if (currentNonce == null) {

			final byte[] result = new byte[8];

			random.nextBytes(result);

			currentNonce = Base64.getEncoder().encodeToString(result);
		}

		return currentNonce;
	}


	private final Set<String> inlineCode = new HashSet<>();

	public void addInline(final String content) {
		logger.fine(() -> "addInline(" + content + ")");
		inlineCode.add(content);
	}

	public Set<String> getInlineCode() {
		return inlineCode;
	}


	private boolean enabled = false;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void enable(final boolean inlineScripts, final boolean unsafeInline) {
		enabled = true;
		this.inlineScripts = inlineScripts;
		this.unsafeInline = unsafeInline;
	}

}

