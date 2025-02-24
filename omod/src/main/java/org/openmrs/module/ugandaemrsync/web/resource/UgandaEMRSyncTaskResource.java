package org.openmrs.module.ugandaemrsync.web.resource;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.*;
import org.apache.commons.logging.Log;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
import org.openmrs.module.webservices.helper.TaskAction;
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
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;

import java.util.*;

@Resource(name = RestConstants.VERSION_1 + "/ugandaemrsynctask", supportedClass = TaskDefinition.class, supportedOpenmrsVersions = {"1.9.* - 9.*"})
public class UgandaEMRSyncTaskResource extends DelegatingCrudResource<TaskDefinition> {

	@Override
	public TaskDefinition newDelegate() {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}

	@Override
	public TaskDefinition save(TaskDefinition TaskDefinition) {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}

	@Override
	public TaskDefinition getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}

	@Override
	public NeedsPaging<TaskDefinition> doGetAll(RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}

	@Override
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.DEFAULT, Representation.FULL);
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("name");
		description.addProperty("taskInstance", Representation.REF);
		description.addProperty("startTime");
		description.addProperty("lastExecutionTime");
		description.addProperty("startTimePattern");
		description.addProperty("started");
		description.addProperty("taskClass");
		return description;
	}

	@Override
	protected void delete(TaskDefinition syncFfhirProfile, String s, RequestContext requestContext) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}

	@Override
	public void purge(TaskDefinition syncFfhirProfile, RequestContext requestContext) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	@Override
	protected PageableResult doSearch(RequestContext context) {
		Collection<TaskDefinition> allSchedules=Context.getService(SchedulerService.class).getRegisteredTasks();
		List<TaskDefinition> relevantSchedules=new ArrayList<>();

		String module = context.getParameter("module");

		for(TaskDefinition taskDefinition:allSchedules){
			if(taskDefinition.getTaskClass().contains("ugandaemr")){
				relevantSchedules.add(taskDefinition);
			}
		}
		return new NeedsPaging<TaskDefinition>(relevantSchedules, context);
	}
}
