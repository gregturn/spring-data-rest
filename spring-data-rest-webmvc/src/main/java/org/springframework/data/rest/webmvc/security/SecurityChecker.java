package org.springframework.data.rest.webmvc.security;

public class SecurityChecker implements AbstractSecurityChecker {

	@Override
	public boolean secured() {
		return true;
	}
}
