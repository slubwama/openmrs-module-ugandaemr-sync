package org.openmrs.module.ugandaemrsync.web.resource;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirCase;
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

@Resource(name = RestConstants.VERSION_1 + "/syncfhircase", supportedClass = SyncFhirCase.class, supportedOpenmrsVersions = {
        "1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*", "2.5.*"})
public class SyncFhirCaseResource extends DelegatingCrudResource<SyncFhirCase> {

    @Override
    public SyncFhirCase newDelegate() {
        return new SyncFhirCase();
    }

    @Override
    public SyncFhirCase save(SyncFhirCase SyncFhirCase) {
        return Context.getService(UgandaEMRSyncService.class).saveSyncFHIRCase(SyncFhirCase);
    }

    @Override
    public SyncFhirCase getByUniqueId(String uniqueId) {
        SyncFhirCase syncFhirCase = null;
        Integer id = null;

        syncFhirCase = Context.getService(UgandaEMRSyncService.class).getSyncFhirCaseByUUDI(uniqueId);
        if (syncFhirCase == null && uniqueId != null) {
            try {
                id = Integer.parseInt(uniqueId);
            } catch (Exception e) {
            }

            if (id != null) {
                syncFhirCase = Context.getService(UgandaEMRSyncService.class).getSyncFhirCaseById(id);
            }
        }

        return syncFhirCase;
    }

    @Override
    public NeedsPaging<SyncFhirCase> doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<SyncFhirCase>(new ArrayList<SyncFhirCase>(Context.getService(UgandaEMRSyncService.class)
                .getAllSyncFhirCase()), context);
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
            description.addProperty("caseIdentifier");
            description.addProperty("patient", Representation.REF);
            description.addProperty("profile", Representation.REF);
            description.addProperty("lastUpdateDate");
            description.addSelfLink();
            return description;
        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("caseIdentifier");
            description.addProperty("patient", Representation.REF);
            description.addProperty("profile", Representation.REF);
            description.addProperty("lastUpdateDate");
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
            description.addProperty("caseIdentifier");
            description.addProperty("patient", Representation.REF);
            description.addProperty("profile", Representation.REF);
            description.addProperty("lastUpdateDate");
            description.addSelfLink();
            return description;
        }
        return null;
    }

    @Override
    protected void delete(SyncFhirCase syncFhirCase, String s, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public void purge(SyncFhirCase syncFhirCase, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("uuid");
        description.addProperty("caseIdentifier");
        description.addProperty("patient", Representation.REF);
        description.addProperty("profile", Representation.REF);
        description.addProperty("lastUpdateDate");

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        String profile = context.getParameter("profile");
        String patient = context.getParameter("patient");

        List<SyncFhirCase> syncFhirProfilesByQuery = null;

        syncFhirProfilesByQuery = ugandaEMRSyncService.getSyncFhirCasesByProfile(ugandaEMRSyncService.getSyncFhirProfileByUUID(profile));

        return new NeedsPaging<SyncFhirCase>(syncFhirProfilesByQuery, context);
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model.property("uuid", new StringProperty()).property("caseIdentifier", new StringProperty())
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
    public Model getCREATEModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model.property("uuid", new StringProperty()).property("caseIdentifier", new StringProperty())
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
        return new ModelImpl().property("uuid", new StringProperty()).property("caseIdentifier", new StringProperty())
                .property("lastUpdateDate", new DateProperty())
                .property("patient", new RefProperty("#/definitions/PatientGetRef"))
                .property("profile", new RefProperty("#/definitions/SyncFhirProfileGetRef"))
                .property("creator", new RefProperty("#/definitions/UserGetRef"))
                .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
                .property("voidedBy", new RefProperty("#/definitions/UserGetRef"));
    }
}
