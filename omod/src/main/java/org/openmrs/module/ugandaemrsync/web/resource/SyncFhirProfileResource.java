package org.openmrs.module.ugandaemrsync.web.resource;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.DateProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
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
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/syncfhirprofile", supportedClass = SyncFhirProfile.class, supportedOpenmrsVersions = {"1.9.* - 9.*"})
public class SyncFhirProfileResource extends DelegatingCrudResource<SyncFhirProfile> {

	@Override
	public SyncFhirProfile newDelegate() {
		return new SyncFhirProfile();
	}

	@Override
	public SyncFhirProfile save(SyncFhirProfile SyncFhirProfile) {
		return Context.getService(UgandaEMRSyncService.class).saveSyncFhirProfile(SyncFhirProfile);
	}

	@Override
	public SyncFhirProfile getByUniqueId(String uniqueId) {
		SyncFhirProfile syncFfhirProfile = null;
		Integer id = null;

		syncFfhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID(uniqueId);
		if (syncFfhirProfile == null && uniqueId != null) {
			try {
				id = Integer.parseInt(uniqueId);
			}
			catch (Exception e) {}

			if (id != null) {
				syncFfhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileById(id);
			}
		}

		return syncFfhirProfile;
	}

	@Override
	public NeedsPaging<SyncFhirProfile> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<SyncFhirProfile>(new ArrayList<SyncFhirProfile>(Context.getService(UgandaEMRSyncService.class)
		        .getAllSyncFhirProfile()), context);
	}

	@Override
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.DEFAULT, Representation.FULL);
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name", Representation.REF);
			description.addProperty("resourceTypes");
			description.addProperty("profileEnabled");
			description.addProperty("patientIdentifierType", Representation.REF);
			description.addProperty("numberOfResourcesInBundle");
			description.addProperty("durationToKeepSyncedResources");
			description.addProperty("generateBundle");
			description.addProperty("isCaseBasedProfile");
			description.addProperty("caseBasedPrimaryResourceType");
			description.addProperty("caseBasedPrimaryResourceTypeId");
			description.addProperty("caseBasedPrimaryResourceType");
			description.addProperty("resourceSearchParameter");
			description.addProperty("conceptSource");
			description.addProperty("url");
			description.addProperty("syncLimit");
			description.addProperty("urlToken");
			description.addProperty("urlUserName");
			description.addProperty("urlPassword");
			description.addProperty("syncDataEverSince");
			description.addProperty("isCaseBasedProfile");
			description.addProperty("dataToSyncStartDate");
			description.addProperty("searchable");
			description.addProperty("searchURL");
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name", Representation.REF);
			description.addProperty("resourceTypes");
			description.addProperty("profileEnabled");
			description.addProperty("patientIdentifierType", Representation.REF);
			description.addProperty("numberOfResourcesInBundle");
			description.addProperty("durationToKeepSyncedResources");
			description.addProperty("generateBundle");
			description.addProperty("isCaseBasedProfile");
			description.addProperty("caseBasedPrimaryResourceType");
			description.addProperty("caseBasedPrimaryResourceTypeId");
			description.addProperty("caseBasedPrimaryResourceType");
			description.addProperty("resourceSearchParameter");
			description.addProperty("conceptSource", Representation.REF);
			description.addProperty("url");
			description.addProperty("syncLimit");
			description.addProperty("urlToken");
			description.addProperty("urlUserName");
			description.addProperty("urlPassword");
			description.addProperty("creator", Representation.REF);
			description.addProperty("dateCreated");
			description.addProperty("changedBy", Representation.REF);
			description.addProperty("dateChanged");
			description.addProperty("voidedBy", Representation.REF);
			description.addProperty("dateVoided");
			description.addProperty("voidReason");
			description.addProperty("syncDataEverSince");
			description.addProperty("isCaseBasedProfile");
			description.addProperty("dataToSyncStartDate");
			description.addProperty("searchable");
			description.addProperty("searchURL");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name", Representation.REF);
			description.addProperty("resourceTypes");
			description.addProperty("profileEnabled");
			description.addProperty("patientIdentifierType", Representation.REF);
			description.addProperty("numberOfResourcesInBundle");
			description.addProperty("durationToKeepSyncedResources");
			description.addProperty("generateBundle");
			description.addProperty("isCaseBasedProfile");
			description.addProperty("caseBasedPrimaryResourceType");
			description.addProperty("caseBasedPrimaryResourceTypeId");
			description.addProperty("resourceSearchParameter");
			description.addProperty("conceptSource", Representation.REF);
			description.addProperty("url");
			description.addProperty("syncLimit");
			description.addProperty("urlToken");
			description.addProperty("urlUserName");
			description.addProperty("urlPassword");
			description.addProperty("syncDataEverSince");
			description.addProperty("isCaseBasedProfile");
			description.addProperty("dataToSyncStartDate");
			description.addProperty("searchable");
			description.addProperty("searchURL");
			description.addSelfLink();
			return description;
		}
		return null;
	}

	@Override
	protected void delete(SyncFhirProfile syncFfhirProfile, String s, RequestContext requestContext) throws ResponseException {

	}

	@Override
	public void purge(SyncFhirProfile syncFfhirProfile, RequestContext requestContext) throws ResponseException {

	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("name");
		description.addProperty("resourceTypes");
		description.addProperty("profileEnabled");
		description.addProperty("patientIdentifierType");
		description.addProperty("numberOfResourcesInBundle");
		description.addProperty("numberOfResourcesInBundle");
		description.addProperty("generateBundle");
		description.addProperty("isCaseBasedProfile");
		description.addProperty("caseBasedPrimaryResourceType");
		description.addProperty("caseBasedPrimaryResourceTypeId");
		description.addProperty("resourceSearchParameter");
		description.addProperty("conceptSource");
		description.addProperty("syncLimit");
		description.addProperty("url");
		description.addProperty("urlToken");
		description.addProperty("urlUserName");
		description.addProperty("urlPassword");
		description.addProperty("syncDataEverSince");
		description.addProperty("isCaseBasedProfile");
		description.addProperty("dataToSyncStartDate");
		description.addProperty("searchable");
		description.addProperty("searchURL");

		return description;
	}

	@Override
	protected PageableResult doSearch(RequestContext context) {
		UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

		String name = context.getParameter("name");
		String uuid = context.getParameter("uuid");

		List<SyncFhirProfile> SyncFhirProfilesByQuery = null;

		SyncFhirProfilesByQuery = ugandaEMRSyncService.getSyncFhirProfileByName(name);

		return new NeedsPaging<SyncFhirProfile>(SyncFhirProfilesByQuery, context);
	}

	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model.property("uuid", new StringProperty())
					.property("name", new StringProperty())
			        .property("resourceTypes", new StringProperty())
					.property("profileEnabled", new BooleanProperty())
			        .property("patientIdentifierType", new StringProperty())
					.property("numberOfResourcesInBundle", new IntegerProperty())
			        .property("durationToKeepSyncedResources", new IntegerProperty())
					.property("generateBundle", new BooleanProperty())
					.property("dataToSyncStartDate", new DateProperty())
					.property("isCaseBasedProfile", new BooleanProperty())
					.property("syncDataEverSince", new BooleanProperty())
			        .property("isCaseBasedProfile", new BooleanProperty())
					.property("caseBasedPrimaryResourceType", new StringProperty())
                    .property("caseBasedPrimaryResourceTypeId", new StringProperty())
					.property("resourceSearchParameter", new StringProperty())
					.property("searchable", new BooleanProperty())
					.property("searchURL", new StringProperty());
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
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model.property("uuid", new StringProperty())
					.property("name", new StringProperty())
                    .property("resourceTypes", new StringProperty())
					.property("profileEnabled", new BooleanProperty())
                    .property("patientIdentifierType", new StringProperty())
					.property("numberOfResourcesInBundle", new IntegerProperty())
                    .property("durationToKeepSyncedResources", new IntegerProperty())
					.property("generateBundle", new BooleanProperty())
                    .property("isCaseBasedProfile", new BooleanProperty())
                    .property("syncDataEverSince", new BooleanProperty())
					.property("dataToSyncStartDate", new DateProperty())
                    .property("isCaseBasedProfile", new BooleanProperty())
					.property("caseBasedPrimaryResourceType", new StringProperty())
                    .property("caseBasedPrimaryResourceTypeId", new StringProperty())
					.property("resourceSearchParameter", new StringProperty())
					.property("searchable", new BooleanProperty())
					.property("searchURL", new StringProperty());

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
		return new ModelImpl().property("uuid", new StringProperty())
				.property("name", new StringProperty())
                .property("resourceTypes", new StringProperty())
				.property("profileEnabled", new BooleanProperty())
                .property("patientIdentifierType", new StringProperty())
				.property("numberOfResourcesInBundle", new IntegerProperty())
                .property("durationToKeepSyncedResources", new IntegerProperty())
                .property("dataToSyncStartDate", new DateProperty())
				.property("generateBundle", new BooleanProperty())
				.property("isCaseBasedProfile", new BooleanProperty())
				.property("syncDataEverSince", new BooleanProperty())
                .property("isCaseBasedProfile", new BooleanProperty())
				.property("caseBasedPrimaryResourceType", new StringProperty())
                .property("caseBasedPrimaryResourceTypeId", new StringProperty())
				.property("resourceSearchParameter", new StringProperty())
				.property("searchable", new BooleanProperty())
				.property("searchURL", new StringProperty())
                .property("patientIdentifierType", new RefProperty("#/definitions/PatientIdentifierTypeGetRef"))
                .property("conceptSource", new RefProperty("#/definitions/ConceptGetRef"))
                .property("creator", new RefProperty("#/definitions/UserGetRef"))
                .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));
	}
}
