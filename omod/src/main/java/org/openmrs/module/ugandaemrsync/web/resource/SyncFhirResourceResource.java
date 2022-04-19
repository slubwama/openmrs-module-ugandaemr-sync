package org.openmrs.module.ugandaemrsync.web.resource;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.BooleanProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.model.SyncFhirResource;
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

@Resource(name = RestConstants.VERSION_1 + "/syncfhirresource", supportedClass = SyncFhirResource.class, supportedOpenmrsVersions = {
        "1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*", "2.5.*"})
public class SyncFhirResourceResource extends DelegatingCrudResource<SyncFhirResource> {

    @Override
    public SyncFhirResource newDelegate() {
        return new SyncFhirResource();
    }

    @Override
    public SyncFhirResource save(SyncFhirResource syncFhirResource) {
        return Context.getService(UgandaEMRSyncService.class).saveFHIRResource(syncFhirResource);
    }

    @Override
    public SyncFhirResource getByUniqueId(String uniqueId) {
        SyncFhirResource syncFfhirProfile = null;
        Integer id = null;

        syncFfhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFhirResourceByUUID(uniqueId);
        if (syncFfhirProfile == null && uniqueId != null) {
            try {
                id = Integer.parseInt(uniqueId);
            } catch (Exception e) {
            }

            if (id != null) {
                syncFfhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFHIRResourceById(id);
            }
        }

        return syncFfhirProfile;
    }

    @Override
    public NeedsPaging<SyncFhirResource> doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<SyncFhirResource>(new ArrayList<SyncFhirResource>(Context.getService(UgandaEMRSyncService.class)
                .getAllFHirResources()), context);
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
            description.addProperty("synced");
            description.addProperty("dateSynced");
            description.addProperty("expiryDate");
            description.addProperty("generatorProfile", Representation.REF);
            description.addProperty("resource");
            description.addSelfLink();
            return description;
        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("synced");
            description.addProperty("dateSynced");
            description.addProperty("expiryDate");
            description.addProperty("generatorProfile", Representation.REF);
            description.addProperty("resource");
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
            description.addProperty("synced");
            description.addProperty("dateSynced");
            description.addProperty("expiryDate");
            description.addProperty("expiryDate");
            description.addProperty("generatorProfile", Representation.REF);
            description.addSelfLink();
            return description;
        }
        return null;
    }

    @Override
    protected void delete(SyncFhirResource syncFfhirProfile, String s, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public void purge(SyncFhirResource syncFfhirProfile, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("uuid");
        description.addProperty("synced");
        description.addProperty("dateSynced");
        description.addProperty("expiryDate");
        description.addProperty("expiryDate");
        description.addProperty("generatorProfile", Representation.REF);
        description.addProperty("resource");

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        String synced = context.getParameter("synced");
        String generatorProfileUUID = context.getParameter("profile");
        SyncFhirProfile generatorProfile = null;

        if (generatorProfileUUID != "" || generatorProfileUUID != null) {
            generatorProfile = ugandaEMRSyncService.getSyncFhirProfileByUUID(generatorProfileUUID);
        }

        List<SyncFhirResource> syncFhirResources;


        syncFhirResources = ugandaEMRSyncService.getSyncFHIRResourceBySyncFhirProfile(generatorProfile, Boolean.parseBoolean(synced));


        return new NeedsPaging<SyncFhirResource>(syncFhirResources, context);
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model.property("uuid", new StringProperty()).property("synced", new BooleanProperty())
                    .property("dateSynced", new DateProperty()).property("expiryDate", new DateProperty());
        }
        if (rep instanceof DefaultRepresentation) {
            model.property("generatorProfile", new RefProperty("#/definitions/SyncFhirProfileGetRef"));

        } else if (rep instanceof FullRepresentation) {
            model.property("generatorProfile", new RefProperty("#/definitions/SyncFhirProfileGetRef"));
        }
        return model;
    }

    @Override
    public Model getCREATEModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model.property("uuid", new StringProperty()).property("synced", new BooleanProperty())
                    .property("dateSynced", new DateProperty()).property("expiryDate", new DateProperty());
        }
        if (rep instanceof DefaultRepresentation) {
            model.property("generatorProfile", new RefProperty("#/definitions/SyncFhirProfileGetRef"))
                    .property("creator", new RefProperty("#/definitions/UserGetRef"))
                    .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                    .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));;

        } else if (rep instanceof FullRepresentation) {
            model.property("generatorProfile", new RefProperty("#/definitions/SyncFhirProfileGetRef"))
                    .property("creator", new RefProperty("#/definitions/UserGetRef"))
                    .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                    .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));
        }
        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        return new ModelImpl().property("uuid", new StringProperty()).property("synced", new BooleanProperty())
                .property("dateSynced", new DateProperty()).property("expiryDate", new DateProperty())
                .property("generatorProfile", new RefProperty("#/definitions/SyncFhirProfileGetRef"))
                .property("creator", new RefProperty("#/definitions/UserGetRef"))
                .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));
    }
}
