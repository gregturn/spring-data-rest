package org.springframework.data.rest.webmvc.security;

public class SpringSecurityChecker implements SecurityChecker {

	@Override
	public boolean secured() {
		return true;
	}
}
