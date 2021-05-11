
package com.antonyudin.faces.csp;


import java.util.Base64;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

import java.security.MessageDigest;

import java.nio.charset.StandardCharsets;


public interface ContentSecurityPolicyGenerator {

	public static class Header implements java.io.Serializable {

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

