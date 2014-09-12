package org.springframework.data.rest.webmvc.gemfire;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.gemfire.mapping.Region;

/**
 * @author Greg Turnquist
 */
@Region("Receipt")
public class Receipt {

	@Id public String id;

	private String saleItem;

	private BigDecimal amount;

	@Version private Long version;

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
