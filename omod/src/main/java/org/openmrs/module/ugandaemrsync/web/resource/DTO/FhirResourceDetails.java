package org.openmrs.module.ugandaemrsync.web.resource.DTO;

import java.util.Date;

public class FhirResourceDetails {
    String name;
    String identifier;
    String status;
    Date dateCreated;
    Date dateSynced;

    int statusCode;

    String patientUuid;


    public FhirResourceDetails(String name, String identifier,int statusCode, String status, Date dateCreated, Date dateSynced) {
        this.name = name;
        this.identifier = identifier;
        this.status = status;
        this.dateCreated = dateCreated;
        this.statusCode = statusCode;
        this.dateSynced = dateSynced;
    }

    public FhirResourceDetails(String name, String identifier,int statusCode, String status, Date dateCreated, Date dateSynced,String patientUuid) {
        this.name = name;
        this.identifier = identifier;
        this.status = status;
        this.dateCreated = dateCreated;
        this.statusCode = statusCode;
        this.dateSynced = dateSynced;
        this.patientUuid = patientUuid;
    }

    public FhirResourceDetails() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Date getDateSynced() {
        return dateSynced;
    }

    public void setDateSynced(Date dateSynced) {
        this.dateSynced = dateSynced;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }
}
