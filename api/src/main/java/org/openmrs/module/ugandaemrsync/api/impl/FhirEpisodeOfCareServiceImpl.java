/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.api.impl;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.formula.functions.T;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.openmrs.PatientProgram;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.impl.BaseFhirService;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.openmrs.module.ugandaemrsync.api.FhirEpisodeOfCareService;
import org.openmrs.module.ugandaemrsync.api.dao.FhirEpisodeOfCareDao;
import org.openmrs.module.ugandaemrsync.api.translators.EpisodeOfCareTranslator;
import org.openmrs.module.ugandaemrsync.api.translators.impl.EpisodeOfCareTranslatorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class FhirEpisodeOfCareServiceImpl extends BaseFhirService<EpisodeOfCare, PatientProgram> implements FhirEpisodeOfCareService {

    @Autowired
    private FhirEpisodeOfCareDao dao;

    @Autowired
    private EpisodeOfCareTranslator<PatientProgram> translator;

    @Autowired
    private SearchQueryInclude<EpisodeOfCare> searchQueryInclude;

    @Autowired
    private SearchQuery<PatientProgram, EpisodeOfCare, FhirEpisodeOfCareDao, EpisodeOfCareTranslator<PatientProgram>, SearchQueryInclude<EpisodeOfCare>> searchQuery;

    @Override
    public EpisodeOfCare get(@Nonnull String uuid) {

        EpisodeOfCare result = null;
        try {
            result = super.get(uuid);
        } catch (ResourceNotFoundException e) {

        }

        return result;
    }

    @Override
    protected FhirDao<PatientProgram> getDao() {
        return this.dao;
    }


    @Override
    protected OpenmrsFhirTranslator<PatientProgram, EpisodeOfCare> getTranslator() {
        return this.translator;
    }


    @Override
    @Transactional(readOnly = true)
    public IBundleProvider searchForEpisodeOfCares(ReferenceAndListParam patient, ReferenceAndListParam type, TokenAndListParam id,
                                                   DateRangeParam lastUpdated, HashSet<Include> includes, HashSet<Include> revIncludes) {
        SearchParameterMap theParams = new SearchParameterMap().addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, lastUpdated)
                .addParameter(FhirConstants.PARTICIPANT_REFERENCE_SEARCH_HANDLER, patient)
                .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, type)
                .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, id)
                .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
                .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes)
                .addParameter(FhirConstants.REVERSE_INCLUDE_SEARCH_HANDLER, revIncludes);
        return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
    }
}
