package org.springframework.data.rest.webmvc.security;

public class NoSecurityChecker implements AbstractSecurityChecker {

	@Override
	public boolean secured() {
		return false;
	}
}
