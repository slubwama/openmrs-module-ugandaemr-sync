package org.openmrs.module.ugandaemrsync.web.resource;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfileLog;
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

@Resource(name = RestConstants.VERSION_1 + "/syncfhirprofilelog", supportedClass = SyncFhirProfileLog.class, supportedOpenmrsVersions = {
        "1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*", "2.5.*"})
public class SyncFhirProfileLogResource extends DelegatingCrudResource<SyncFhirProfileLog> {

    @Override
    public SyncFhirProfileLog newDelegate() {
        return new SyncFhirProfileLog();
    }

    @Override
    public SyncFhirProfileLog save(SyncFhirProfileLog SyncFhirProfileLog) {
        return Context.getService(UgandaEMRSyncService.class).saveSyncFhirProfileLog(SyncFhirProfileLog);
    }

    @Override
    public SyncFhirProfileLog getByUniqueId(String uniqueId) {
        SyncFhirProfileLog syncFfhirProfile = null;
        Integer id = null;

        syncFfhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileLogByUUID(uniqueId);
        if (syncFfhirProfile == null && uniqueId != null) {
            try {
                id = Integer.parseInt(uniqueId);
            } catch (Exception e) {
            }

            if (id != null) {
                syncFfhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileLogById(id);
            }
        }

        return syncFfhirProfile;
    }

    @Override
    public NeedsPaging<SyncFhirProfileLog> doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<SyncFhirProfileLog>(new ArrayList<SyncFhirProfileLog>(Context.getService(UgandaEMRSyncService.class)
                .getAllSyncFhirProfileLog()), context);
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
            description.addProperty("resourceType");
            description.addProperty("profile", Representation.REF);
            description.addProperty("lastGenerationDate");
            description.addProperty("numberOfResources");
            description.addSelfLink();
            return description;
        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("resourceType");
            description.addProperty("profile", Representation.REF);
            description.addProperty("lastGenerationDate");
            description.addProperty("numberOfResources");
            description.addProperty("creator", Representation.REF);
            description.addProperty("dateCreated");
            description.addProperty("changedBy", Representation.REF);
            description.addProperty("dateChanged");
            description.addProperty("voidedBy", Representation.REF);
            description.addProperty("dateVoided");
            description.addProperty("voidReason");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (rep instanceof RefRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("resourceType");
            description.addProperty("profile", Representation.REF);
            description.addProperty("lastGenerationDate");
            description.addProperty("numberOfResources");
            description.addSelfLink();
            return description;
        }
        return null;
    }

    @Override
    protected void delete(SyncFhirProfileLog syncFhirProfile, String s, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public void purge(SyncFhirProfileLog syncFhirProfile, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("uuid");
        description.addProperty("resourceType");
        description.addProperty("profile", Representation.REF);
        description.addProperty("lastGenerationDate");
        description.addProperty("numberOfResources");

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        String profile = context.getParameter("profile");

        List<SyncFhirProfileLog> syncFhirProfilesByQuery = null;

        syncFhirProfilesByQuery = ugandaEMRSyncService.getSyncFhirProfileLogByProfile(ugandaEMRSyncService.getSyncFhirProfileByUUID(profile));

        return new NeedsPaging<SyncFhirProfileLog>(syncFhirProfilesByQuery, context);
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model.property("uuid", new StringProperty()).property("resourceType", new StringProperty())
                    .property("numberOfResources", new StringProperty())
                    .property("lastUpdateDate", new StringProperty());
        }
        if (rep instanceof DefaultRepresentation) {
            model.property("profile", new RefProperty("#/definitions/SyncFhirProfileGetRef"))
                    .property("creator", new RefProperty("#/definitions/UserGetRef"))
                    .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                    .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));

        } else if (rep instanceof FullRepresentation) {
            model.property("profile", new RefProperty("#/definitions/SyncFhirProfileGetRef"))
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
            model.property("uuid", new StringProperty()).property("resourceType", new StringProperty())
                    .property("numberOfResources", new StringProperty())
                    .property("lastUpdateDate", new StringProperty());
        }
        if (rep instanceof DefaultRepresentation) {
            model.property("patient", new RefProperty("#/definitions/PatientGetRef"))
                    .property("profile", new RefProperty("#/definitions/SyncFhirProfileGetRef"))
                    .property("creator", new RefProperty("#/definitions/UserGetRef"))
                    .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                    .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));

        } else if (rep instanceof FullRepresentation) {
            model.property("patient", new RefProperty("#/definitions/PatientGetRef"))
                    .property("profile", new RefProperty("#/definitions/SyncFhirProfileGetRef"))
                    .property("creator", new RefProperty("#/definitions/UserGetRef"))
                    .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                    .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));
        }
        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        return new ModelImpl().property("uuid", new StringProperty()).property("resourceType", new StringProperty())
                .property("numberOfResources", new StringProperty())
                .property("lastUpdateDate", new StringProperty())
                .property("profile", new RefProperty("#/definitions/SyncFhirProfileGetRef"))
                .property("creator", new RefProperty("#/definitions/UserGetRef"))
                .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));
    }
}
