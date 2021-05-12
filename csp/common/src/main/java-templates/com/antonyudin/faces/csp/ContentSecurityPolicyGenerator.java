
package com.antonyudin.faces.csp;


public interface ContentSecurityPolicyGenerator {

	public static class Header implements java.io.Serializable {
//	public record Header(
//		String name, String value
//	) {

		public Header(final String name, final String value) {
			this.name = name;
			this.value = value;
		}


		private final String name;

		public String getName() {
			return name;
		}


		private final String value;

		public String getValue() {
			return value;
		}

	}


	public Header generate(final ContentSecurityPolicy policy) throws java.lang.Exception;

}

