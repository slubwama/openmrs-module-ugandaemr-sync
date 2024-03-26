package org.openmrs.module.ugandaemrsync.web.resource;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.RefProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
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

@Resource(name = RestConstants.VERSION_1 + "/synctask", supportedClass = SyncTask.class, supportedOpenmrsVersions = {"1.9.* - 9.*"})
public class SyncTaskResource extends DelegatingCrudResource<SyncTask> {

	@Override
	public SyncTask newDelegate() {
		return new SyncTask();
	}

	@Override
	public SyncTask save(SyncTask SyncTask	) {
		return Context.getService(UgandaEMRSyncService.class).saveSyncTask(SyncTask);
	}

	@Override
	public SyncTask getByUniqueId(String uniqueId) {
		SyncTask SyncTask = null;
		Integer id = null;

		SyncTask = Context.getService(UgandaEMRSyncService.class).getSyncTaskByUUID(uniqueId);
		if (SyncTask == null && uniqueId != null) {
			try {
				id = Integer.parseInt(uniqueId);
			}
			catch (Exception e) {}

			if (id != null) {
				SyncTask = Context.getService(UgandaEMRSyncService.class).getSyncTaskById(id);
			}
		}

		return SyncTask;
	}

	@Override
	protected void delete(SyncTask delegate, String reason, RequestContext context) throws ResponseException {

	}

	@Override
	public void purge(SyncTask delegate, RequestContext context) throws ResponseException {

	}

	@Override
	public NeedsPaging<SyncTask> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<SyncTask>(new ArrayList<SyncTask>(Context.getService(UgandaEMRSyncService.class)
		        .getAllSyncTask()), context);
	}

	@Override
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.DEFAULT, Representation.FULL);
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {


		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("syncTask");
			description.addProperty("status");
			description.addProperty("statusCode");
			description.addProperty("sentToUrl");
			description.addProperty("dateSent");
			description.addProperty("requireAction");
			description.addProperty("actionCompleted");

			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("syncTaskType", Representation.REF);
			description.addProperty("syncTask");
			description.addProperty("status");
			description.addProperty("statusCode");
			description.addProperty("sentToUrl");
			description.addProperty("dateSent");
			description.addProperty("requireAction");
			description.addProperty("actionCompleted");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("syncTaskType", Representation.REF);
			description.addProperty("syncTask");
			description.addProperty("status");
			description.addProperty("statusCode");
			description.addProperty("sentToUrl");
			description.addProperty("dateSent");
			description.addProperty("requireAction");
			description.addProperty("actionCompleted");
			description.addSelfLink();
			return description;
		}
		return null;
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("syncTaskType", Representation.REF);
		description.addProperty("syncTask");
		description.addProperty("status");
		description.addProperty("statusCode");
		description.addProperty("sentToUrl");
		description.addProperty("dateSent");
		description.addProperty("requireAction");
		description.addProperty("actionCompleted");
		return description;
	}


	@Override
	protected PageableResult doSearch(RequestContext context) {
		UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

		String type = context.getParameter("type");

		List<SyncTask> SyncTasksByQuery = null;
		if(type !=null){
			SyncTasksByQuery = ugandaEMRSyncService.getSyncTasksByType(ugandaEMRSyncService.getSyncTaskTypeByUUID(type));
		}

		return new NeedsPaging<SyncTask>(SyncTasksByQuery, context);
	}

	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model.property("syncTask", new StringProperty())
					.property("statusCode", new IntegerProperty())
					.property("sentToUrl", new StringProperty())
					.property("dateSent", new DateProperty())
					.property("requireAction", new BooleanProperty())
					.property("actionCompleted", new BooleanProperty())
					.property("actionCompleted", new StringProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model.property("syncTask", new StringProperty())
					.property("status", new StringProperty())
					.property("syncTaskType", new RefProperty("#/definitions/SyncTaskTypeGetRef"))
					.property("statusCode", new IntegerProperty())
					.property("sentToUrl", new StringProperty())
					.property("dateSent", new DateProperty())
					.property("requireAction", new BooleanProperty())
					.property("actionCompleted", new BooleanProperty())
					.property("creator", new RefProperty("#/definitions/UserGetRef"))
					.property("changedBy", new RefProperty("#/definitions/UserGetRef"))
					.property("voidedBy", new RefProperty("#/definitions/UserGetRef"));

		} else if (rep instanceof FullRepresentation) {
			model.property("syncTask", new StringProperty())
					.property("status", new StringProperty())
					.property("syncTaskType", new RefProperty("#/definitions/SyncTaskTypeGetRef"))
					.property("statusCode", new IntegerProperty())
					.property("sentToUrl", new StringProperty())
					.property("dateSent", new DateProperty())
					.property("requireAction", new BooleanProperty())
					.property("actionCompleted", new BooleanProperty())
					.property("creator", new RefProperty("#/definitions/UserGetRef"))
					.property("changedBy", new RefProperty("#/definitions/UserGetRef"))
					.property("voidedBy", new RefProperty("#/definitions/UserGetRef"));;
		}
		return model;
	}

	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model.property("syncTask", new StringProperty())
					.property("status", new StringProperty())
					.property("statusCode", new IntegerProperty())
					.property("sentToUrl", new StringProperty())
					.property("dateSent", new DateProperty())
					.property("requireAction", new BooleanProperty())
					.property("actionCompleted", new BooleanProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model.property("syncTask", new StringProperty())
					.property("status", new StringProperty())
					.property("syncTaskType", new RefProperty("#/definitions/SyncTaskTypeGetRef"))
					.property("statusCode", new IntegerProperty())
					.property("sentToUrl", new StringProperty())
					.property("dateSent", new DateProperty())
					.property("requireAction", new BooleanProperty())
					.property("actionCompleted", new BooleanProperty())
					.property("creator", new RefProperty("#/definitions/UserGetRef"))
					.property("changedBy", new RefProperty("#/definitions/UserGetRef"))
					.property("voidedBy", new RefProperty("#/definitions/UserGetRef"));

		} else if (rep instanceof FullRepresentation) {
			model.property("syncTask", new StringProperty())
					.property("status", new StringProperty())
					.property("syncTaskType", new RefProperty("#/definitions/SyncTaskTypeGetRef"))
					.property("statusCode", new IntegerProperty())
					.property("sentToUrl", new StringProperty())
					.property("dateSent", new DateProperty())
					.property("requireAction", new BooleanProperty())
					.property("actionCompleted", new BooleanProperty())
					.property("creator", new RefProperty("#/definitions/UserGetRef"))
					.property("changedBy", new RefProperty("#/definitions/UserGetRef"))
					.property("voidedBy", new RefProperty("#/definitions/UserGetRef"));;
		}
		return model;
	}

	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model.property("syncTask", new StringProperty())
					.property("status", new StringProperty())
					.property("statusCode", new IntegerProperty())
					.property("sentToUrl", new StringProperty())
					.property("dateSent", new DateProperty())
					.property("requireAction", new BooleanProperty())
					.property("actionCompleted", new BooleanProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model.property("syncTask", new StringProperty())
					.property("status", new StringProperty())
					.property("syncTaskType", new RefProperty("#/definitions/SyncTaskTypeGetRef"))
					.property("statusCode", new IntegerProperty())
					.property("sentToUrl", new StringProperty())
					.property("dateSent", new DateProperty())
					.property("requireAction", new BooleanProperty())
					.property("actionCompleted", new BooleanProperty())
					.property("creator", new RefProperty("#/definitions/UserGetRef"))
					.property("changedBy", new RefProperty("#/definitions/UserGetRef"))
					.property("voidedBy", new RefProperty("#/definitions/UserGetRef"));

		} else if (rep instanceof FullRepresentation) {
			model.property("syncTask", new StringProperty())
					.property("status", new StringProperty())
					.property("syncTaskType", new RefProperty("#/definitions/SyncTaskTypeGetRef"))
					.property("statusCode", new IntegerProperty())
					.property("sentToUrl", new StringProperty())
					.property("dateSent", new DateProperty())
					.property("requireAction", new BooleanProperty())
					.property("actionCompleted", new BooleanProperty())
					.property("creator", new RefProperty("#/definitions/UserGetRef"))
					.property("changedBy", new RefProperty("#/definitions/UserGetRef"))
					.property("voidedBy", new RefProperty("#/definitions/UserGetRef"));;
		}
		return model;
	}
}
