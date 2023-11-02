package org.openmrs.module.ugandaemrsync.web.resource;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.model.SyncFhirResource;
import org.openmrs.module.ugandaemrsync.model.SyncFhirResource;
import org.openmrs.module.ugandaemrsync.web.resource.DTO.FhirResourceDetails;
import org.openmrs.module.ugandaemrsync.web.resource.mapper.ConverterHelper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/syncfhirresourcedetails", supportedClass = SyncFhirResource.class, supportedOpenmrsVersions = {
        "1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*", "2.5.*" })
public class SyncFhirResourceDetailsResource extends DelegatingCrudResource<SyncFhirResource> {

	@Override
	public SyncFhirResource newDelegate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SyncFhirResource save(SyncFhirResource SyncFhirResource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SyncFhirResource getByUniqueId(String uniqueId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NeedsPaging<SyncFhirResource> doGetAll(RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.DEFAULT, Representation.FULL);
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("identifier");
			description.addProperty("status");
			description.addProperty("statusCode");
			description.addProperty("dateCreated");
			description.addProperty("patientUuid");


			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("identifier");
			description.addProperty("status");
			description.addProperty("statusCode");
			description.addProperty("dateCreated");
			description.addProperty("patientUuid");
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("identifier");
			description.addProperty("status");
			description.addProperty("statusCode");
			description.addProperty("dateCreated");
			description.addProperty("patientUuid");
			return description;
		}
		return null;
	}

	@Override
	protected void delete(SyncFhirResource SyncFhirResource, String s, RequestContext requestContext) throws ResponseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void purge(SyncFhirResource SyncFhirResource, RequestContext requestContext) throws ResponseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("name");
		description.addProperty("identifier");
		description.addProperty("status");

		return description;
	}

	@Override
	protected PageableResult doSearch(RequestContext context) {
		UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

		String profileId = context.getParameter("profile");

		String startDateString = context.getParameter("startDate");
		String endDateString = context.getParameter("endDate");

		SyncFhirProfile syncFhirProfile = ugandaEMRSyncService.getSyncFhirProfileByUUID(profileId);
		List<SyncFhirResource> SyncFhirResourcesByQuery = new ArrayList<>();
		if(startDateString != null &&endDateString != null) {

			try {
				if (!validateDateIsValidFormat(endDateString) || !validateDateIsValidFormat(startDateString)) {
					SimpleObject message = new SimpleObject();
					message.put("error", "date parsed " + endDateString + "is not valid");

				}

				if (syncFhirProfile != null) {
					Date synceDateFrom = DateUtil.parseYmd(startDateString);
					Date synceDateTo = DateUtil.parseYmd(endDateString);

					SyncFhirResourcesByQuery = ugandaEMRSyncService.getSyncFHIRResourceBySyncFhirProfile(syncFhirProfile, startDateString, endDateString);
				}

			} catch (Exception ex) {
			}
		}
		System.out.println("size of sync resources"+ SyncFhirResourcesByQuery.size());
		List<FhirResourceDetails> fhirResourceDetails = new ArrayList<>();
		if(!SyncFhirResourcesByQuery.isEmpty()){
			fhirResourceDetails = ConverterHelper.convertSyncFhirResources(SyncFhirResourcesByQuery);
		}

		return new NeedsPaging<FhirResourceDetails>(fhirResourceDetails, context);
	}

	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model.property("uuid", new StringProperty()).property("name", new StringProperty())
			        .property("SyncFhirResourceType", new StringProperty()).property("profileEnabled", new BooleanProperty())
			        .property("patientIdentifierType", new StringProperty()).property("numberOfResourcesInBundle", new IntegerProperty())
			        .property("durationToKeepSyncedResources", new IntegerProperty()).property("generateBundle", new BooleanProperty())
			        .property("caseBasedProfile", new BooleanProperty()).property("caseBasedPrimaryResourceType", new StringProperty())
                    .property("caseBasedPrimaryResourceTypeId", new StringProperty()) .property("resourceSearchParameter", new StringProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model.property("SyncFhirResourceType", new RefProperty("#/SyncFhirResourceType"))
			        .property("conceptSource", new RefProperty("#/definitions/ConceptGetRef"))
			        .property("creator", new RefProperty("#/definitions/UserGetRef"))
			        .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
			        .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));

		} else if (rep instanceof FullRepresentation) {
            model.property("patientIdentifierType", new RefProperty("#/definitions/PatientIdentifierTypeGetRef"))
                    .property("conceptSource", new RefProperty("#/definitions/ConceptGetRef"))
			        .property("creator", new RefProperty("#/definitions/UserGetRef"))
			        .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
			        .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));
		}
		return model;
	}

	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model.property("uuid", new StringProperty()).property("name", new StringProperty())
                    .property("resourceTypes", new StringProperty()).property("profileEnabled", new BooleanProperty())
                    .property("patientIdentifierType", new StringProperty()).property("numberOfResourcesInBundle", new IntegerProperty())
                    .property("durationToKeepSyncedResources", new IntegerProperty()).property("generateBundle", new BooleanProperty())
                    .property("caseBasedProfile", new BooleanProperty()).property("caseBasedPrimaryResourceType", new StringProperty())
                    .property("caseBasedPrimaryResourceTypeId", new StringProperty()) .property("resourceSearchParameter", new StringProperty());
		}
		if (rep instanceof DefaultRepresentation) {
            model.property("patientIdentifierType", new RefProperty("#/definitions/PatientIdentifierTypeGetRef"))
                    .property("conceptSource", new RefProperty("#/definitions/ConceptGetRef"))
                    .property("creator", new RefProperty("#/definitions/UserGetRef"))
                    .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                    .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));

		} else if (rep instanceof FullRepresentation) {
            model.property("patientIdentifierType", new RefProperty("#/definitions/PatientIdentifierTypeGetRef"))
                    .property("conceptSource", new RefProperty("#/definitions/ConceptGetRef"))
                    .property("creator", new RefProperty("#/definitions/UserGetRef"))
                    .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                    .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));
		}
		return model;
	}

	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl().property("uuid", new StringProperty()).property("name", new StringProperty())
                .property("resourceTypes", new StringProperty()).property("profileEnabled", new BooleanProperty())
                .property("patientIdentifierType", new StringProperty()).property("numberOfResourcesInBundle", new IntegerProperty())
                .property("durationToKeepSyncedResources", new IntegerProperty()).property("generateBundle", new BooleanProperty())
                .property("caseBasedProfile", new BooleanProperty()).property("caseBasedPrimaryResourceType", new StringProperty())
                .property("caseBasedPrimaryResourceTypeId", new StringProperty()) .property("resourceSearchParameter", new StringProperty())
                .property("patientIdentifierType", new RefProperty("#/definitions/PatientIdentifierTypeGetRef"))
                .property("conceptSource", new RefProperty("#/definitions/ConceptGetRef"))
                .property("creator", new RefProperty("#/definitions/UserGetRef"))
                .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));
	}

	public Boolean validateDateIsValidFormat(String date) {
		try {
			DateUtil.parseYmd(date);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
}
