package org.springframework.data.rest.webmvc.security;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.data.rest.core.invoke.RepositoryInvoker;
import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.util.SimpleMethodInvocation;

public class SpringSecurityChecker implements SecurityChecker {

	private MethodSecurityInterceptor smi;

	@Override
	public boolean secured() {
		return true;
	}

	@Override
	public boolean hasAccess(RootResourceInformation resourceInformation, HttpMethod method) {

		RepositoryInvoker invoker = resourceInformation.getInvoker();
		Object repository = resourceInformation.getInvoker().getRepository();

		switch (method) {
			case GET:
				return hasAccess(repository, invoker.getFindAllMethod()) ||
						hasAccess(repository, invoker.getFindOneMethod());
			case POST:
			case PUT:
			case PATCH:
				return hasAccess(repository, invoker.getSaveMethod());
			case DELETE:
				return hasAccess(repository, invoker.getDeleteMethod());
		}

		return false;
	}

	private boolean hasAccess(Object target, Method method, Class<?>... argTypes) {

		MethodInvocation mi = new NoOpMethodInvocation(target, method);
		try {
			smi.invoke(mi);
			return true;
		} catch (AccessDeniedException denied) {
			return false;
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	private class NoOpMethodInvocation extends SimpleMethodInvocation {

		public NoOpMethodInvocation(Object targetObject, Method method,
									Object... arguments) {
			super(targetObject, method, arguments);
		}

		public Object proceed() throws Throwable {
			return null;
		}
	}


	public MethodSecurityInterceptor getSmi() {
		return smi;
	}

	public void setSmi(MethodSecurityInterceptor smi) {
		this.smi = smi;
	}

}
