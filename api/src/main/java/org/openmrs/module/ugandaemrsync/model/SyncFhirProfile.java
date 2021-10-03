package org.openmrs.module.ugandaemrsync.model;

import org.hibernate.annotations.Type;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.ConceptSource;
import org.openmrs.PatientIdentifierType;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import java.io.Serializable;

@Entity(name = "ugandaemrsync.SyncFhirProfile")
@Table(name = "sync_fhir_profile")
public class SyncFhirProfile extends BaseOpenmrsData implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "sync_fhir_profile_id")
    private Integer syncFhirProfileId;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "resource_types", length = 255)
    private String resourceTypes;

    @Column(name = "profile_enabled")
    private Boolean profileEnabled;

    @ManyToOne
    @JoinColumn(name = "patient_identifier_type")
    private PatientIdentifierType patientIdentifierType;

    @Column(name = "number_of_resources_in_bundle", length = 11)
    private Integer numberOfResourcesInBundle;

    @Column(name = "duration_to_keep_synced_resources", length = 11)
    private Integer durationToKeepSyncedResources;

    @Column(name = "generate_bundle")
    private Boolean generateBundle;

    @Column(name = "is_case_based_profile")
    private Boolean isCaseBasedProfile;

    @Column(name = "case_based_primary_resource_type", length = 255)
    private String caseBasedPrimaryResourceType;

    @Column(name = "case_based_primary_resource_type_id", length = 255)
    private String caseBasedPrimaryResourceTypeId;

    @Column(name = "resource_search_parameter", length = 5000)
    @Type(type = "text")
    private String resourceSearchParameter;

    @ManyToOne
    @JoinColumn(name = "concept_source")
    private ConceptSource conceptSource;

    @Column(name = "url_end_point")
    private String url;

    @Column(name = "url_token")
    private String urlToken;

    @Column(name = "url_username")
    private String urlUserName;

    @Column(name = "url_password")
    private String urlPassword;

    public int getSyncFhirProfileId() {
        return syncFhirProfileId;
    }

    public void setSyncFhirProfileId(Integer syncFhirProfileId) {
        this.syncFhirProfileId = syncFhirProfileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getProfileEnabled() {
        return profileEnabled;
    }

    public void setProfileEnabled(Boolean profileEnabled) {
        this.profileEnabled = profileEnabled;
    }

    public String getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(String resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public String getResourceSearchParameter() {
        return resourceSearchParameter;
    }

    public void setResourceSearchParameter(String resourceSearchParameter) {
        this.resourceSearchParameter = resourceSearchParameter;
    }

    public Boolean getGenerateBundle() {
        return generateBundle;
    }

    public void setGenerateBundle(Boolean generateBundle) {
        this.generateBundle = generateBundle;
    }


    public Integer getNumberOfResourcesInBundle() {
        return numberOfResourcesInBundle;
    }

    public void setNumberOfResourcesInBundle(Integer numberOfResourcesInBundle) {
        this.numberOfResourcesInBundle = numberOfResourcesInBundle;
    }

    public PatientIdentifierType getPatientIdentifierType() {
        return patientIdentifierType;
    }

    public void setPatientIdentifierType(PatientIdentifierType patientIdentifierType) {
        this.patientIdentifierType = patientIdentifierType;
    }

    public Integer getDurationToKeepSyncedResources() {
        return durationToKeepSyncedResources;
    }

    public void setDurationToKeepSyncedResources(Integer durationToKeepSyncedResources) {
        this.durationToKeepSyncedResources = durationToKeepSyncedResources;
    }

    public Boolean getCaseBasedProfile() {
        return isCaseBasedProfile;
    }

    public void setCaseBasedProfile(Boolean caseBasedProfile) {
        isCaseBasedProfile = caseBasedProfile;
    }

    public String getCaseBasedPrimaryResourceType() {
        return caseBasedPrimaryResourceType;
    }

    public void setCaseBasedPrimaryResourceType(String caseBasedPrimaryResourceType) {
        this.caseBasedPrimaryResourceType = caseBasedPrimaryResourceType;
    }

    public String getCaseBasedPrimaryResourceTypeId() {
        return caseBasedPrimaryResourceTypeId;
    }

    public void setCaseBasedPrimaryResourceTypeId(String caseBasedPrimaryResourceTypeId) {
        this.caseBasedPrimaryResourceTypeId = caseBasedPrimaryResourceTypeId;
    }

    public ConceptSource getConceptSource() {
        return conceptSource;
    }

    public void setConceptSource(ConceptSource conceptSource) {
        this.conceptSource = conceptSource;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlToken() {
        return urlToken;
    }

    public void setUrlToken(String urlToken) {
        this.urlToken = urlToken;
    }

    public String getUrlUserName() {
        return urlUserName;
    }

    public void setUrlUserName(String urlUserName) {
        this.urlUserName = urlUserName;
    }

    public String getUrlPassword() {
        return urlPassword;
    }

    public void setUrlPassword(String urlPassword) {
        this.urlPassword = urlPassword;
    }

    @Override
    public Integer getId() {
        return this.syncFhirProfileId;
    }

    @Override
    public void setId(Integer id) {
    }
}
