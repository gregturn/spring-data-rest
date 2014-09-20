package org.springframework.data.rest.webmvc.security;

import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.http.HttpMethod;

public class NoSecurityChecker implements SecurityChecker {

	@Override
	public boolean secured() {
		return false;
	}

	@Override
	public boolean hasAccess(RootResourceInformation resourceInformation, HttpMethod method) {
		return true;
	}
}
