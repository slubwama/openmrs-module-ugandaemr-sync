package org.openmrs.module.ugandaemrsync.model;

import org.openmrs.BaseOpenmrsData;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "ugandaemrsync.SyncFhirProfileLog")
@Table(name = "sync_fhir_profile_log")
public class SyncFhirProfileLog extends BaseOpenmrsData implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "profile_log_id", length = 11)
    private Integer profileLogId;

    @Column(name = "resource_type", length = 255)
    private String resourceType;

    @ManyToOne
    @JoinColumn(name = "profile")
    private SyncFhirProfile profile;

    @Column(name = "last_generation_date", nullable = false)
    private Date lastGenerationDate;

    @Column(name = "number_of_resources", length = 11)
    private Integer numberOfResources;


    public Integer getProfileLogId() {
        return profileLogId;
    }

    public void setProfileLogId(Integer profileLogId) {
        this.profileLogId = profileLogId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public SyncFhirProfile getProfile() {
        return profile;
    }

    public void setProfile(SyncFhirProfile profile) {
        this.profile = profile;
    }

    public Date getLastGenerationDate() {
        return lastGenerationDate;
    }

    public void setLastGenerationDate(Date lastGenerationDate) {
        this.lastGenerationDate = lastGenerationDate;
    }


    public Integer getNumberOfResources() {
        return numberOfResources;
    }

    public void setNumberOfResources(Integer numberOfResources) {
        this.numberOfResources = numberOfResources;
    }

    @Override
    public Integer getId() {
        return getProfileLogId();
    }

    @Override
    public void setId(Integer id) {
        this.profileLogId = id;
    }
}
