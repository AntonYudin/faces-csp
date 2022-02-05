
package com.antonyudin.faces.csp;


import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import java.security.MessageDigest;

import java.nio.charset.StandardCharsets;

import ${jee.el}.ELManager;
import ${jee.el}.ValueExpression;
import ${jee.el}.StandardELContext;


public class DefaultContentSecurityPolicyGenerator implements ContentSecurityPolicyGenerator, java.io.Serializable {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		DefaultContentSecurityPolicyGenerator.class.getName()
	);


	public static ContentSecurityPolicyGenerator newInstance(
		final boolean enabled,
		final boolean reportOnly,
		final String template,
		final List<String> contentToHash
	) throws java.lang.Exception {
		return new DefaultContentSecurityPolicyGenerator(
			enabled,
			reportOnly,
			template,
			contentToHash
		);
	}


	private final boolean enabled;
	private final boolean reportOnly;
	private final String template;
	private final List<String> contentToHash;


	protected DefaultContentSecurityPolicyGenerator(
		final boolean enabled,
		final boolean reportOnly,
		final String template,
		final List<String> contentToHash
	) throws java.lang.Exception {
		logger.info("DefaultContentSecurityPolicyGenerator(" + enabled + ", " + reportOnly + ", " + template + ")");
		this.enabled = enabled;
		this.reportOnly = reportOnly;
		this.template = template;
		this.contentToHash = contentToHash;
		valueExpression = elManager.getExpressionFactory().createValueExpression(
			elManager.getELContext(), template, String.class
		);
	}


	private final ELManager elManager = new ELManager();
	private final ValueExpression valueExpression;


	@Override
	public Header generate(final ContentSecurityPolicy policy, final Map<String, Object> values) throws java.lang.Exception {

		if (!enabled)
			return null;

		if ((template == null) || (template.trim().length() <= 0))
			return null;

		if (contentToHash != null) {
			for (var content: contentToHash)
				policy.addInline(content);
		}


		final var elContext = new StandardELContext(elManager.getExpressionFactory());

		final var resolver = elContext.getELResolver();

		resolver.setValue(elContext, null, "env", values);

//		final var result = new StringBuilder();

//		result.append(template);

		if (!policy.isUnsafeInline()) {

			final var nonce = policy.getNonce();

			resolver.setValue(elContext, null, "nonce", "'nonce-" + nonce + "'");

//			substitute(result, "${nonce}", (nonce != null? "'nonce-" + nonce + "'": ""));

//			logger.fine(() -> "with nonce: " + result);
		} else {

			resolver.setValue(elContext, null, "nonce", "'unsafe-inline'");

//			substitute(result, "${nonce}", "'unsafe-inline'");
		}


		if (!policy.isUnsafeInline()) {

			final var h = new StringBuilder();

			final var inlineCode = policy.getInlineCode();

			if (inlineCode != null) {

				final var digest = MessageDigest.getInstance("SHA-256");
				final var encoder = Base64.getEncoder();

				for (var code: inlineCode) {

					if (h.length() > 0)
						h.append(" ");

					h.append("'");

					h.append("sha256-");

					h.append(
						encoder.encodeToString(
							digest.digest(code.getBytes(StandardCharsets.UTF_8))
						)
					);

					digest.reset();
		
					h.append("'");
				}
			}

			resolver.setValue(elContext, null, "hashes", h.toString());

//			substitute(result, "${hashes}", h.toString());
		
//			logger.fine(() -> "with hashes: " + result);
		} else {

			resolver.setValue(elContext, null, "hashes", "");

//			substitute(result, "${hashes}", "");
		}

		logger.fine("getting value ...");

		final var result = new StringBuilder(String.class.cast(valueExpression.getValue(elContext)));

		for (;;) {
			final var index = result.indexOf("\t");

			if (index < 0)
				break;

			result.deleteCharAt(index);
		}


		logger.fine(() -> "result: [" + result + "]");

		if (result.length() > 0) {

			return (
				new Header(
					reportOnly?
					"Content-Security-Policy-Report-Only":
					"Content-Security-Policy",
					result.toString()
				)
			);
		}

		return null;
	}

	protected void substitute(final StringBuilder content, final String pattern, final String value) {
		for (;;) {
			final var index = content.indexOf(pattern);
			if (index < 0)
				break;
			content.replace(index, index + pattern.length(), value);
		}
	}

}

