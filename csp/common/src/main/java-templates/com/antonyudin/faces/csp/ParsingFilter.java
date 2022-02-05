
package com.antonyudin.faces.csp;


import ${jee.servlet}.http.HttpServletRequest;
import ${jee.servlet}.http.HttpServletResponse;
import ${jee.servlet}.http.HttpServletResponseWrapper;
import ${jee.servlet}.http.HttpFilter;

import ${jee.servlet}.ServletOutputStream;
import ${jee.servlet}.ServletException;
import ${jee.servlet}.FilterChain;
import ${jee.servlet}.WriteListener;

import ${jee.servlet}.annotation.WebFilter;


//@WebFilter(urlPatterns = "*.xhtml")
public class ParsingFilter extends AbstractFilter {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		ParsingFilter.class.getName()
	);


	public ParsingFilter() {
	}


	public static class Response extends HttpServletResponseWrapper {

		private final HttpServletRequest request;
		private final HttpServletResponse wrapped;
		private final ContentSecurityPolicy policy;
		private final ContentSecurityPolicyGenerator generator;
		private final java.io.ByteArrayOutputStream copy = new java.io.ByteArrayOutputStream();


		public Response(
			final HttpServletRequest request,
			final HttpServletResponse wrapped,
			final ContentSecurityPolicy policy,
			final ContentSecurityPolicyGenerator generator
		) throws java.security.NoSuchAlgorithmException, java.io.IOException {

			super(wrapped);

			this.request = request;
			this.wrapped = wrapped;
			this.policy = policy;
			this.generator = generator;
		}


		protected void setPolicy() throws java.lang.Exception {

			logger.fine(() -> "setPolicy()");

			final var header = generator.generate(
				policy, getEnvironment(request)
			);

			if (header != null)
				wrapped.addHeader(header.getName(), header.getValue());
		}

		public void finish() throws java.lang.Exception {

			printWriter.flush();

			if (policy.isEnabled()) {
				if (policy.isInlineScripts()) {

					logger.fine(() -> "finishing with inlineScripts ...");

					setPolicy();

			//		logger.info("content: [" + new String(copy.toByteArray(), "UTF-8") + "]");
					copy.writeTo(wrapped.getOutputStream());
					copy.reset();

					//wrapped.getOutputStream().flush();
				} else
					logger.fine(() -> "finishing without inlineScripts ...");
			}
		}


		private final ServletOutputStream servletOutputStream = new ServletOutputStream() {

			private final String patternStart = "=\"mojarra.";
			private final int skipPatternCharacters = 2;
			private final String patternEnd = "\"";

			private final byte[] buffer = new byte[patternStart.length()];
			private final StringBuilder code = new StringBuilder();

			private int currentIndex= 0;

			private boolean matched = false;
			private boolean first = true;

			@Override
			public void write(final int value) throws java.io.IOException {
			//	logger.info("write: [" + value + "]");

				try {
					if (first) {
						logger.fine(() -> "policy: [" + policy + "]");
						logger.fine(() -> "policy.enabled: " + policy.isEnabled());
						logger.fine(() -> "policy.inlineScripts: " + policy.isInlineScripts());
						logger.fine(() -> "policy.unsafeInline: " + policy.isUnsafeInline());
					}

					if (!policy.isEnabled()) {
						wrapped.getOutputStream().write(value);
						return;
					}

				
					if (!policy.isInlineScripts()) {

						if (first) {
							try {
								setPolicy();
							} catch (java.lang.Exception exception) {
								throw new java.io.IOException(exception);
							}
						}

						wrapped.getOutputStream().write(value);

					} else {

						copy.write(value);

						try {
							if (currentIndex >= buffer.length)
								currentIndex = 0;

							buffer[currentIndex] = (byte) value;

						//	logger.info("buffer[" + currentIndex + "]: [" + new String(buffer, "UTF-8") + "]");

							if (matched) {
								if (match(buffer, currentIndex, patternEnd)) {
						//			logger.info("stop match!");
									matched = false;
						//			logger.info("code: [" + code + "]");
									policy.addInline(code.toString());
									code.delete(0, code.length());
								} else {
									code.append(((char) (buffer[currentIndex] & 0XFF)));
								}
							} else if (match(buffer, currentIndex, patternStart)) {
								//logger.info("match!");
								matched = true;
								code.append(patternStart.substring(skipPatternCharacters));
							}
						} catch (java.lang.Exception exception) {
							throw new IllegalArgumentException(exception);
						}

						currentIndex++;
					}
				} finally {
					first = false;
				}
			}


			protected boolean match(final byte[] buffer, final int startIndex, final String pattern) {

				var currentIndex = startIndex;

				final var line = new StringBuilder();

				for (int i = 0; i < buffer.length; i++) {

					if (i >= pattern.length())
						break;

					if (currentIndex < 0)
						currentIndex = buffer.length - 1;

					final var c = ((char) (buffer[currentIndex] & 0XFF));

					if (pattern.charAt(pattern.length() - i - 1) != c)
						return false;

					//line.append((char) (buffer[currentIndex] & 0XFF));

					currentIndex--;
				}

				return true;
			}

			@Override
			public void setWriteListener(final WriteListener writeListener) {
				try {
					wrapped.getOutputStream().setWriteListener(writeListener);
				} catch (java.lang.Exception exception) {
					throw new IllegalArgumentException(exception);
				}
			}

			@Override
			public boolean isReady() {
				try {
					return wrapped.getOutputStream().isReady();
				} catch (java.lang.Exception exception) {
					throw new IllegalArgumentException(exception);
				}
			}

		};


		@Override
		public ServletOutputStream getOutputStream() {
			return servletOutputStream;
		}


		private final java.io.PrintWriter printWriter = new java.io.PrintWriter(servletOutputStream);

		@Override
		public java.io.PrintWriter getWriter() {
		//	logger.info("getWriter()");
			return printWriter;
		}

	}


	protected void doFilter(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final FilterChain chain
	) throws java.io.IOException, ServletException {

		logger.fine(() -> "doFilter(" + request + ", " + response + ") ...");

		try {
			final Response r = new Response(request, response, policy, generator);

			chain.doFilter(request, r);
			
			r.finish();

		} catch (java.lang.Exception exception) {
			throw new ServletException(exception);
		}

		logger.fine(() -> "doFilter(" + request + ", " + response + ") ... done");
	}

}

