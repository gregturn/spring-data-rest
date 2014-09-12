package org.springframework.data.rest.webmvc.gemfire;

import org.springframework.data.repository.CrudRepository;

/**
 * A repository to manage {@link Receipt}s.
 *
 * @author Pablo Lozano
 */

public interface ReceiptRepository extends CrudRepository<Receipt, String> {

}
