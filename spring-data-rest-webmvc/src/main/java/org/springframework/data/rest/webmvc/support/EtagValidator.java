package org.springframework.data.rest.webmvc.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.BeanWrapper;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.ValueConstants;

/**
 * An ETag validator that verifies concurrency issues and the validity of the Etag/If-Match header values. This is
 * used to implement optimistic locking.
 * 
 * @author Pablo Lozano
 * @author Greg Turnquist
 */

public class EtagValidator {

	private ObjectMapper objectMapper;
	private ConversionService conversionService;

	private static final Logger LOG = LoggerFactory.getLogger(EtagValidator.class);

	public EtagValidator(ObjectMapper objectMapper, ConversionService conversionService) {

		this.objectMapper = objectMapper;
		this.conversionService = conversionService;
	}

	/**
	 * Compare the given eTag with the entity's version property. If they are different, an
	 * {@link org.springframework.dao.OptimisticLockingFailureException} is thrown.
	 *
	 * @param requestEtag
	 * @param resourceInformation
	 * @param domainObject
	 * @return
	 */
	public boolean isEtagValueValid(String requestEtag, RootResourceInformation resourceInformation, Object domainObject) {

		if (!requestEtag.equals(ValueConstants.DEFAULT_NONE)) {

			final String entityEtag = getVersionInformation(resourceInformation.getPersistentEntity(), domainObject);
			if (!requestEtag.equals(entityEtag)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Sets the Etag in the header based on the domain object's {@literal Version}. If the domain object does NOT
	 * contain a {@literal Version} property it will return with the headers as is.
	 *
	 * @param headers
	 * @param persistentEntityResource
	 */
	public void addEtagHeader(HttpHeaders headers, PersistentEntityResource persistentEntityResource) {

		String version = getVersionInformation(persistentEntityResource.getPersistentEntity(),
				persistentEntityResource.getContent());

		if (version != null) {
			headers.setETag(version);
		}
	}

	/**
	 * Returns the {@literal Version} property of a domain object wrapped in quotes.
	 * Returns null if it doesn't contains the property.
	 *
	 * @param persistentEntity
	 * @param domainObject
	 * @return
	 */
	private String getVersionInformation(PersistentEntity persistentEntity, Object domainObject) {

		if (persistentEntity.hasVersionProperty()) {

			Object version = BeanWrapper.create(domainObject, conversionService)
					.getProperty(persistentEntity.getVersionProperty());

			try {
				return "\"" + objectMapper.writeValueAsString(version) + "\"";
			} catch (JsonProcessingException e) {
				LOG.error("Creation of Etag header failed", e);
			}
		}

		return null;
	}

	/**
	 * Throws an {@link OptimisticLockingFailureException} related to the mismatch of the ETags
	 */
	public void throwOptimisticLockingException() {
		throw new OptimisticLockingFailureException("Invalid If-Match version provided. The resource has changed " +
				"since last request");
	}
}
