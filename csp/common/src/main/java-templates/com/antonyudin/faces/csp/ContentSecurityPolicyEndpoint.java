
package com.antonyudin.faces.csp;


import java.util.Base64;
import java.util.Random;

import ${jee.servlet}.ServletException;

import ${jee.servlet}.annotation.WebServlet;

import ${jee.servlet}.http.HttpServlet;
import ${jee.servlet}.http.HttpServletRequest;
import ${jee.servlet}.http.HttpServletResponse;


@WebServlet(
	name = "EndpointServlet",
	urlPatterns = {
		"/csp-report"
	}
)
public class ContentSecurityPolicyEndpoint extends HttpServlet {

	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		ContentSecurityPolicyEndpoint.class.getName()
	);



	private final static int MAX_LENGTH = 1024 * 10;


	@Override
	protected void doPost(
		final HttpServletRequest request,
		final HttpServletResponse response
	) throws ServletException, java.io.IOException {

		try {
			final var content = new StringBuilder();

			content.append("CSP-report[");

			content.append(request.getRemoteAddr());

			final var xForwardedFor = request.getHeader("X-Forwarded-For");

			if ((xForwardedFor != null) && (xForwardedFor.trim().length() > 0)) {
				content.append(", ");
				content.append(xForwardedFor);
			}

			content.append("]: ");

			final var length = request.getContentLength();

			final var buffer = (
				((length > 0) && (length <= MAX_LENGTH))?
				new byte[length]:
				new byte[MAX_LENGTH]
			);

			final var read = request.getInputStream().read(buffer);

			if (read > 0) {
				content.append(
					new String(
						buffer, 0, read,
						java.nio.charset.StandardCharsets.UTF_8
					)
				);
			}

			logger.fine(content::toString);

		} catch (Exception exception) {
			logger.warning("exception: " + exception);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

}

