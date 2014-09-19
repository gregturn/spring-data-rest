package org.springframework.data.rest.webmvc.security;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.annotation.Secured;

@Secured("ROLE_USER")
@RepositoryRestResource(collectionResourceRel = "people", path = "people")
public interface SecurePersonRepository extends CrudRepository<Person, Long> {

	@Secured("ROLE_ADMIN")
	@Override
	void deleteAll();
}
