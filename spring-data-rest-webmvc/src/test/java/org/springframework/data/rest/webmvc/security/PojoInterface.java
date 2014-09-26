package org.springframework.data.rest.webmvc.security;

import org.springframework.security.access.annotation.Secured;

@Secured("ROLE_ADMIN")
public interface PojoInterface {

}
