package org.openmrs.module.ugandaemrsync.mapper;

import java.io.Serializable;

public class Identifier implements Serializable {

	String identifier;

	String identifierType;

	String identifierTypeName;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(String identifierType) {
		this.identifierType = identifierType;
	}

	public String getIdentifierTypeName() {
		return identifierTypeName;
	}

	public void setIdentifierTypeName(String identifierTypeName) {
		this.identifierTypeName = identifierTypeName;
	}
}
