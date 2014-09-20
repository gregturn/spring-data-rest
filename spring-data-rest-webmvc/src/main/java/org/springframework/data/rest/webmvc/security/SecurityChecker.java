package org.springframework.data.rest.webmvc.security;

import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.http.HttpMethod;

public interface SecurityChecker {

	public boolean secured();

	public boolean hasAccess(RootResourceInformation resourceInformation, HttpMethod method);
}
