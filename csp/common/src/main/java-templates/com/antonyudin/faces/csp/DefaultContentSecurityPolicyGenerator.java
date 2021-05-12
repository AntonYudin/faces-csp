
package com.antonyudin.faces.csp;


import java.util.Base64;

import java.security.MessageDigest;

import java.nio.charset.StandardCharsets;


public class DefaultContentSecurityPolicyGenerator implements ContentSecurityPolicyGenerator, java.io.Serializable {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		DefaultContentSecurityPolicyGenerator.class.getName()
	);


	public static ContentSecurityPolicyGenerator newInstance(
		final boolean enabled,
		final boolean reportOnly,
		final String template
	) {
		return new DefaultContentSecurityPolicyGenerator(
			enabled,
			reportOnly,
			template
		);
	}


	private final boolean enabled;
	private final boolean reportOnly;
	private final String template;


	protected DefaultContentSecurityPolicyGenerator(
		final boolean enabled,
		final boolean reportOnly,
		final String template
	) {
		logger.info("DefaultContentSecurityPolicyGenerator(" + enabled + ", " + reportOnly + ", " + template + ")");
		this.enabled = enabled;
		this.reportOnly = reportOnly;
		this.template = template;
	}


	@Override
	public Header generate(final ContentSecurityPolicy policy) throws java.lang.Exception {

		if (!enabled)
			return null;

		if ((template == null) || (template.trim().length() <= 0))
			return null;

		final var result = new StringBuilder();

		result.append(template);

		if (!policy.isUnsafeInline()) {

			final var nonce = policy.getNonce();

			substitute(result, "${nonce}", (nonce != null? "'nonce-" + nonce + "'": ""));

			logger.fine(() -> "with nonce: " + result);
		} else {
			substitute(result, "${nonce}", "'unsafe-inline'");
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

			substitute(result, "${hashes}", h.toString());
		
			logger.fine(() -> "with hashes: " + result);
		} else {
			substitute(result, "${hashes}", "");
		}

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

