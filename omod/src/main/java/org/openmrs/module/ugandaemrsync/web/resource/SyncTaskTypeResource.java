package org.openmrs.module.ugandaemrsync.web.resource;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.DateProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
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

@Resource(name = RestConstants.VERSION_1 + "/synctasktype", supportedClass = SyncTaskType.class, supportedOpenmrsVersions = {"1.9.* - 9.*"})
public class SyncTaskTypeResource extends DelegatingCrudResource<SyncTaskType> {

	@Override
	public SyncTaskType newDelegate() {
		return new SyncTaskType();
	}

	@Override
	public SyncTaskType save(SyncTaskType syncTaskType	) {
		return Context.getService(UgandaEMRSyncService.class).saveSyncTaskType(syncTaskType);
	}

	@Override
	public SyncTaskType getByUniqueId(String uniqueId) {
		SyncTaskType syncTaskType = null;
		Integer id = null;

		syncTaskType = Context.getService(UgandaEMRSyncService.class).getSyncTaskTypeByUUID(uniqueId);
		if (syncTaskType == null && uniqueId != null) {
			try {
				id = Integer.parseInt(uniqueId);
			}
			catch (Exception e) {}

			if (id != null) {
				syncTaskType = Context.getService(UgandaEMRSyncService.class).getSyncTaskTypeById(id);
			}
		}

		return syncTaskType;
	}

	@Override
	public NeedsPaging<SyncTaskType> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<SyncTaskType>(new ArrayList<SyncTaskType>(Context.getService(UgandaEMRSyncService.class)
		        .getAllSyncTaskType()), context);
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
			description.addProperty("name");
			description.addProperty("dataType");
			description.addProperty("url");

			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("dataType");
			description.addProperty("url");
			description.addProperty("urlToken");
			description.addProperty("urlUserName");
			description.addProperty("urlPassword");
			description.addProperty("tokenExpiryDate");
			description.addProperty("tokenType");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("dataType");
			description.addProperty("url");
			description.addSelfLink();
			return description;
		}
		return null;
	}

	@Override
	protected void delete(SyncTaskType syncTaskType, String s, RequestContext requestContext) throws ResponseException {

	}

	@Override
	public void purge(SyncTaskType syncTaskType, RequestContext requestContext) throws ResponseException {

	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("name", Representation.REF);
		description.addProperty("dataType");
		description.addProperty("url");
		description.addProperty("urlUserName");
		description.addProperty(" urlPassword");
		description.addProperty("urlToken");
		description.addProperty("tokenExpiryDate");
		description.addProperty("tokenType");
		description.addProperty("tokenRefreshKey");

		return description;
	}

	@Override
	protected PageableResult doSearch(RequestContext context) {
		UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

		String name = context.getParameter("name");
		String uuid = context.getParameter("uuid");

		List<SyncTaskType> SyncTaskTypesByQuery = null;

		if(name !=null){
			SyncTaskTypesByQuery = ugandaEMRSyncService.getSyncTaskTypeByName(name);
		}

		if(uuid !=null){
			SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(uuid);
			SyncTaskTypesByQuery.add(syncTaskType);
		}


		return new NeedsPaging<SyncTaskType>(SyncTaskTypesByQuery, context);
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
			        .property("isCaseBasedProfile", new BooleanProperty())
					.property("caseBasedPrimaryResourceType", new StringProperty())
                    .property("caseBasedPrimaryResourceTypeId", new StringProperty())
					.property("resourceSearchParameter", new StringProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model.property("patientIdentifierType", new RefProperty("#/definitions/PatientIdentifierTypeGetRef"))
			        .property("conceptSource", new RefProperty("#/definitions/ConceptGetRef"))
			        .property("creator", new RefProperty("#/definitions/UserGetRef"))
			        .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
			        .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));

		} else if (rep instanceof FullRepresentation) {
            model.property("patientIdentifierType", new RefProperty("#/definitions/PatientIdentifierTypeGetRef"))
					.property("urlToken", new StringProperty())
					.property("tokenExpiryDate", new DateProperty())
					.property("tokenType", new StringProperty())
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
					.property("caseBasedPrimaryResourceType", new StringProperty())
                    .property("caseBasedPrimaryResourceTypeId", new StringProperty())
					.property("resourceSearchParameter", new StringProperty());
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
                    .property("voidedBy", new RefProperty("#/definitions/UserGetRef"))
					.property("urlToken", new StringProperty())
					.property("tokenExpiryDate", new DateProperty())
					.property("tokenType", new StringProperty())
					.property("tokenRefreshKey", new StringProperty());
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
				.property("generateBundle", new BooleanProperty())
                .property("isCaseBasedProfile", new BooleanProperty())
				.property("caseBasedPrimaryResourceType", new StringProperty())
                .property("caseBasedPrimaryResourceTypeId", new StringProperty())
				.property("resourceSearchParameter", new StringProperty())
                .property("patientIdentifierType", new RefProperty("#/definitions/PatientIdentifierTypeGetRef"))
                .property("conceptSource", new RefProperty("#/definitions/ConceptGetRef"))
                .property("creator", new RefProperty("#/definitions/UserGetRef"))
                .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                .property("voidedBy", new RefProperty("#/definitions/UserGetRef"))
				.property("urlToken", new StringProperty())
				.property("tokenExpiryDate", new DateProperty())
				.property("tokenType", new StringProperty())
				.property("tokenRefreshKey", new StringProperty());
	}
}
