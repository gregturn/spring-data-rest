package org.springframework.data.rest.webmvc.redis;

import org.springframework.data.annotation.Id;

public class Employee {

	@Id private Long id;

	private String firstName;
	private String lastName;
	private String title;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
