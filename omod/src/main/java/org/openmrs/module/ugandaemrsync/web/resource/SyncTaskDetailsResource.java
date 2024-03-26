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
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.web.resource.DTO.SyncTaskDetails;
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

@Resource(name = RestConstants.VERSION_1 + "/synctaskdetails", supportedClass = SyncTaskDetails.class, supportedOpenmrsVersions = {"1.9.* - 9.*"})
public class SyncTaskDetailsResource extends DelegatingCrudResource<SyncTaskDetails> {

	@Override
	public SyncTaskDetails newDelegate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SyncTaskDetails save(SyncTaskDetails SyncTask) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SyncTaskDetails getByUniqueId(String uniqueId) {
		SyncTask SyncTask = null;
		Integer id = null;
		SyncTaskDetails details = null;

		SyncTask = Context.getService(UgandaEMRSyncService.class).getSyncTaskByUUID(uniqueId);
		if (SyncTask == null && uniqueId != null) {
			try {
				id = Integer.parseInt(uniqueId);
			}
			catch (Exception e) {}

			if (id != null) {
				SyncTask = Context.getService(UgandaEMRSyncService.class).getSyncTaskById(id);
			}
			details = ConverterHelper.convertSyncTaskDetails(SyncTask);
		}

		return details;
	}

	@Override
	public NeedsPaging<SyncTaskDetails> doGetAll(RequestContext context) throws ResponseException {
		List<SyncTask> syncTasks = Context.getService(UgandaEMRSyncService.class)
				.getAllSyncTask();
		List<SyncTaskDetails> syncTaskDetails = new ArrayList<>();
		syncTaskDetails = ConverterHelper.convertSyncTasks(syncTasks);
		return new NeedsPaging<SyncTaskDetails>(syncTaskDetails, context);
	}

	@Override
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.DEFAULT, Representation.FULL);
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("identifier");
			description.addProperty("status");
			description.addProperty("statusCode");
			description.addProperty("dateCreated");
			description.addProperty("comment");
			description.addProperty("patientUuid");
			return description;
	}

	@Override
	protected void delete(SyncTaskDetails SyncTaskDetails, String s, RequestContext requestContext) throws ResponseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void purge(SyncTaskDetails syncTask, RequestContext requestContext) throws ResponseException {
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

		String syncTaskTypeUuid = context.getParameter("synctasktype");

		String startDateString = context.getParameter("startDate");
		String endDateString = context.getParameter("endDate");

		SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(syncTaskTypeUuid);
		List<SyncTask> syncTasksByQuery = new ArrayList<>();
		if(startDateString != null &&endDateString != null) {

			try {
				if (!validateDateIsValidFormat(endDateString) || !validateDateIsValidFormat(startDateString)) {
					SimpleObject message = new SimpleObject();
					message.put("error", "date parsed " + endDateString + "is not valid");

				}

				if (syncTaskType != null) {
					Date synceDateFrom = DateUtil.parseYmd(startDateString);
					Date synceDateTo = DateUtil.parseYmd(endDateString);
					synceDateTo = DateUtil.getEndOfDay(synceDateTo);

					syncTasksByQuery = ugandaEMRSyncService.getSyncTasksByType(syncTaskType, synceDateFrom, synceDateTo);
				}

			} catch (Exception ex) {
			}
		}else{
			syncTasksByQuery = ugandaEMRSyncService.getSyncTasksByType(syncTaskType);
		}
		List<SyncTaskDetails> syncTaskDetailsList = new ArrayList<>();
		if(!syncTasksByQuery.isEmpty()){
			for (SyncTask syncTask : syncTasksByQuery) {
				SyncTaskDetails syncTaskDetails = ConverterHelper.convertSyncTaskDetails(syncTask);
				syncTaskDetailsList.add(syncTaskDetails);
			}
		}

		return new NeedsPaging<SyncTaskDetails>(syncTaskDetailsList, context);
	}

	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model.property("uuid", new StringProperty()).property("name", new StringProperty())
			        .property("syncTaskType", new StringProperty()).property("profileEnabled", new BooleanProperty())
			        .property("patientIdentifierType", new StringProperty()).property("numberOfResourcesInBundle", new IntegerProperty())
			        .property("durationToKeepSyncedResources", new IntegerProperty()).property("generateBundle", new BooleanProperty())
			        .property("isCaseBasedProfile", new BooleanProperty()).property("caseBasedPrimaryResourceType", new StringProperty())
                    .property("caseBasedPrimaryResourceTypeId", new StringProperty()) .property("resourceSearchParameter", new StringProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model.property("syncTaskType", new RefProperty("#/syncTaskType"))
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
                    .property("isCaseBasedProfile", new BooleanProperty()).property("caseBasedPrimaryResourceType", new StringProperty())
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
