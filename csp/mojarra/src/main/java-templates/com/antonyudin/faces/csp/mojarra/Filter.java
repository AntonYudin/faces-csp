
package com.antonyudin.faces.csp.mojarra;



import ${jee.inject}.Inject;

import ${jee.servlet}.http.HttpServletRequest;
import ${jee.servlet}.http.HttpServletResponse;
import ${jee.servlet}.http.HttpServletResponseWrapper;
import ${jee.servlet}.http.HttpFilter;

import ${jee.servlet}.ServletOutputStream;
import ${jee.servlet}.ServletException;
import ${jee.servlet}.FilterChain;
import ${jee.servlet}.FilterConfig;
import ${jee.servlet}.WriteListener;

import ${jee.servlet}.annotation.WebFilter;

import com.antonyudin.faces.csp.AbstractFilter;
import com.antonyudin.faces.csp.ContentSecurityPolicy;
import com.antonyudin.faces.csp.ContentSecurityPolicyGenerator;


//@WebFilter(urlPatterns = "*.xhtml")
public class Filter extends AbstractFilter {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		Filter.class.getName()
	);


	public Filter() {
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
								logger.log(java.util.logging.Level.SEVERE, exception.toString(), exception);
								throw new java.io.IOException(exception);
							}
						}

						wrapped.getOutputStream().write(value);

					} else {
						copy.write(value);
					}

				} finally {
					first = false;
				}
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
			logger.log(java.util.logging.Level.SEVERE, exception.toString(), exception);
			throw new ServletException(exception);
		}

		logger.fine(() -> "doFilter(" + request + ", " + response + ") ... done");
	}

}

