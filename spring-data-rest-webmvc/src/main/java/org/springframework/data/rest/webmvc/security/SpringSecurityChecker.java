package org.springframework.data.rest.webmvc.security;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.reflect.MethodUtils;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.invoke.CrudRepositoryInvoker;
import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.util.SimpleMethodInvocation;
import org.springframework.util.ReflectionUtils;

public class SpringSecurityChecker implements SecurityChecker {

	private MethodSecurityInterceptor smi;

	@Override
	public boolean secured() {
		return true;
	}

	@Override
	public boolean hasAccess(RootResourceInformation resourceInformation, HttpMethod method) {

		System.out.println("Do you have access to " + method.name() + "?");

		if (CrudRepositoryInvoker.class.isAssignableFrom(resourceInformation.getInvoker().getClass())) {
			CrudRepositoryInvoker invoker = (CrudRepositoryInvoker) resourceInformation.getInvoker();
			try {
				Field repositoryField = ReflectionUtils.findField(invoker.getClass(), "repository");
				repositoryField.setAccessible(true);
				System.out.println(repositoryField);
				CrudRepository<Object, Serializable> repository = (CrudRepository<Object, Serializable>) repositoryField.get(invoker);
				System.out.println(repository);

				switch (method) {
					case GET:
						return hasAccess(repository, "findAll");
					case POST:
						return true;
					case PUT:
						return true;
					case PATCH:
						return true;
					case DELETE:
						return hasAccess(repository, "deleteAll");
				}
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		}

		return false;
	}

	private boolean hasAccess(Object target, String methodName, Class<?>... argTypes) throws Throwable {

		Method method = MethodUtils.getAccessibleMethod(target.getClass(), methodName, argTypes);
		MethodInvocation mi = new NoOpMethodInvocation(target, method);
		try {
			smi.invoke(mi);
			System.out.println("YES!!!!");
			return true;
		} catch (AccessDeniedException denied) {
			System.out.println("No.");
			return false;
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
