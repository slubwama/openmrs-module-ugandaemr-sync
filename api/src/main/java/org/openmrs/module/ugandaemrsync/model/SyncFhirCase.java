package org.openmrs.module.ugandaemrsync.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "ugandaemrsync.SyncFHIRCase")
@Table(name = "sync_fhir_case")
public class SyncFhirCase extends BaseOpenmrsData implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "case_id",length = 11)
    private Integer caseId;

    @Column(name = "case_identifier",length = 255)
    private String caseIdentifier;

    @ManyToOne
    @JoinColumn(name = "patient")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "profile")
    private SyncFhirProfile profile;

    @Column(name = "last_date_updated")
    private Date lastUpdateDate;

    public Integer getCaseId() {
        return caseId;
    }

    public void setCaseId(Integer caseId) {
        this.caseId = caseId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getCaseIdentifier() {
        return caseIdentifier;
    }

    public void setCaseIdentifier(String caseIdentifier) {
        this.caseIdentifier = caseIdentifier;
    }

    public SyncFhirProfile getProfile() {
        return profile;
    }

    public void setProfile(SyncFhirProfile profile) {
        this.profile = profile;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public Integer getId() {
        return this.getCaseId();
    }

    @Override
    public void setId(Integer id) {
        this.caseId = id;
    }
}
