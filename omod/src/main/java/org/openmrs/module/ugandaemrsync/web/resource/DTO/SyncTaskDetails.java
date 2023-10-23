package org.openmrs.module.ugandaemrsync.web.resource.DTO;

import java.util.Date;

public class SyncTaskDetails {
    String name;
    String identifier;
    String status;
    int statusCode;
    Date dateCreated;

    String comment;


    public SyncTaskDetails(String name, String identifier,int statusCode, String status, Date dateCreated) {
        this.name = name;
        this.identifier = identifier;
        this.status = status;
        this.dateCreated = dateCreated;
        this.statusCode = statusCode;
    }

    public SyncTaskDetails(String name, String identifier, String status, int statusCode, Date dateCreated, String comment) {
        this.name = name;
        this.identifier = identifier;
        this.status = status;
        this.statusCode = statusCode;
        this.dateCreated = dateCreated;
        this.comment = comment;
    }

    public SyncTaskDetails() {
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
