package org.springframework.data.rest.webmvc.neo4j;

import java.math.BigDecimal;

import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 * @author Greg Turnquist
 */
@NodeEntity
public class Receipt {

	@GraphId public Long id;

	private String saleItem;

	private BigDecimal amount;

	@Version private Long version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSaleItem() {
		return saleItem;
	}

	public void setSaleItem(String saleItem) {
		this.saleItem = saleItem;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
