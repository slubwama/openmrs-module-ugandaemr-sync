package org.openmrs.module.ugandaemrsync.providers.r4;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.openmrs.module.ugandaemrsync.api.FhirEpisodeOfCareService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;

public class EpisodeOfCareProvider implements IResourceProvider {
    @Autowired
    private FhirEpisodeOfCareService episodeOfCareService;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return EpisodeOfCare.class;
    }

    @Read
    public EpisodeOfCare getEpisodeOfCareByUuid(@IdParam @Nonnull IdType id) {
        EpisodeOfCare episodeOfCare = episodeOfCareService.get(id.getIdPart());
        if (episodeOfCare == null) {
            throw new ResourceNotFoundException("Could not find episodeOfCare with Id " + id.getIdPart());
        }
        return episodeOfCare;
    }

    @Create
    @SuppressWarnings("unused")
    public MethodOutcome createEpisodeOfCare(@ResourceParam EpisodeOfCare episodeOfCare) {
        return FhirProviderUtils.buildCreate(episodeOfCareService.create(episodeOfCare));
    }

    @Update
    @SuppressWarnings("unused")
    public MethodOutcome updateEpisodeOfCare(@IdParam IdType id, @ResourceParam EpisodeOfCare episodeOfCare) {
        if (id == null || id.getIdPart() == null) {
            throw new InvalidRequestException("id must be specified to update");
        }

        return FhirProviderUtils.buildUpdate(episodeOfCareService.update(id.getIdPart(), episodeOfCare));
    }

    @Delete
    @SuppressWarnings("unused")
    public OperationOutcome deleteEpisodeOfCare(@IdParam @Nonnull IdType id) {
        org.hl7.fhir.r4.model.EpisodeOfCare episodeOfCare = episodeOfCareService.delete(id.getIdPart());
        if (episodeOfCare == null) {
            throw new ResourceNotFoundException("Could not find episodeOfCare to delete with id " + id.getIdPart());
        }
        return FhirProviderUtils.buildDelete(episodeOfCare);
    }

    @History
    @SuppressWarnings("unused")
    public List<Resource> getEpisodeOfCareHistoryById(@IdParam @Nonnull IdType id) {
        EpisodeOfCare episodeOfCare = episodeOfCareService.get(id.getIdPart());
        if (episodeOfCare == null) {
            throw new ResourceNotFoundException("Could not find episodeOfCare with Id " + id.getIdPart());
        }
        return episodeOfCare.getContained();
    }

    @Search
    public IBundleProvider searchEpisodeOfCare(@OptionalParam(name = EpisodeOfCare.SP_CARE_MANAGER, chainWhitelist = {"", org.hl7.fhir.dstu3.model.Practitioner.SP_IDENTIFIER,
            org.hl7.fhir.dstu3.model.Practitioner.SP_GIVEN, org.hl7.fhir.dstu3.model.Practitioner.SP_FAMILY,
            org.hl7.fhir.dstu3.model.Practitioner.SP_NAME}, targetTypes = org.hl7.fhir.dstu3.model.Practitioner.class) ReferenceAndListParam participantReference,

                                               @OptionalParam(name = EpisodeOfCare.SP_DATE) DateRangeParam date,
                                               @OptionalParam(name = EpisodeOfCare.SP_IDENTIFIER) TokenAndListParam identifier) {

        return episodeOfCareService.searchForEpisodeOfCares(participantReference,null ,identifier, date, null,null);
    }

}
