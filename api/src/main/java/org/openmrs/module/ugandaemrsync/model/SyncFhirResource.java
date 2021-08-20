package org.openmrs.module.ugandaemrsync.model;


import org.hibernate.annotations.Type;
import org.openmrs.BaseOpenmrsData;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import java.util.Date;

@Entity(name = "ugandaemrsync.SyncFHIRResources")
@Table(name = "sync_fhir_resource")
public class SyncFhirResource extends BaseOpenmrsData {

    @Id
    @GeneratedValue
    @Column(name = "resource_id", length = 11)
    private int resourceId;

    @Column(name = "synced")
    private Boolean synced;

    @Column(name = "date_synced")
    private Date dateSynced;

    @Column(name = "expiry_date")
    private Date expiryDate;

    @ManyToOne
    @JoinColumn(name = "generator_profile")
    private SyncFhirProfile generatorProfile;

    @Column(name = "resource", length = 1000000)
    @Type(type="text")
    private String resource;


    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public Boolean getSynced() {
        return synced;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced;
    }

    public Date getDateSynced() {
        return dateSynced;
    }

    public void setDateSynced(Date dateSynced) {
        this.dateSynced = dateSynced;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }


    public SyncFhirProfile getGeneratorProfile() {
        return generatorProfile;
    }

    public void setGeneratorProfile(SyncFhirProfile generatorProfile) {
        this.generatorProfile = generatorProfile;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public Integer getId() {
        return this.resourceId;
    }

    @Override
    public void setId(Integer id) {
        this.resourceId = id;
    }
}
