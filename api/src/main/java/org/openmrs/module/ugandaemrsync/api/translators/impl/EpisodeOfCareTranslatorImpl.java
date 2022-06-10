/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientProgram;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.PatientIdentifierTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.ugandaemrsync.api.translators.EpisodeOfCareTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.GP_DHIS2;

@Component
@Setter(AccessLevel.PACKAGE)
public class EpisodeOfCareTranslatorImpl implements EpisodeOfCareTranslator<PatientProgram> {

    @Autowired
    private PatientReferenceTranslator patientReferenceTranslator;

    @Autowired
    private PatientIdentifierTranslator patientIdentifierTranslator;

    @Autowired
    private ConceptTranslator conceptTranslator;


    @Override
    public EpisodeOfCare toFhirResource(@Nonnull PatientProgram patientProgram) {

        Period period = new Period();
        period.setStart(patientProgram.getDateEnrolled());
        period.setEnd(patientProgram.getDateCompleted());
        List<CodeableConcept> type = new ArrayList<>();
        type.add(conceptTranslator.toFhirResource(patientProgram.getProgram().getConcept()));

        EpisodeOfCare episodeOfCare = new EpisodeOfCare();
        episodeOfCare.setId(patientProgram.getUuid());
        episodeOfCare.setStatus(getStatus(patientProgram));
        episodeOfCare.setIdentifier(getEpisodeOfCareIdentifer(patientProgram));
        episodeOfCare.setManagingOrganization(createOrganizationReference());
        episodeOfCare.setPatient(patientReferenceTranslator.toFhirResource(patientProgram.getPatient()));
        episodeOfCare.setPeriod(period);
        episodeOfCare.setType(type);
        return episodeOfCare;
    }

    @Override
    public PatientProgram toOpenmrsType(@Nonnull EpisodeOfCare episodeOfCare) {
        return null;
    }

    @Override
    public PatientProgram toOpenmrsType(@Nonnull PatientProgram existingEncounter, @Nonnull EpisodeOfCare episodeOfCare) {
        return null;
    }

    protected Reference createOrganizationReference() {
        String healthCenterIdentifier = Context.getAdministrationService().getGlobalProperty(GP_DHIS2);
        String healthCenterName = Context.getLocationService().getLocationByUuid("629d78e9-93e5-43b0-ad8a-48313fd99117").getName();

        Reference reference = new Reference().setReference(FhirConstants.ORGANIZATION + "/" + healthCenterIdentifier)
                .setType(FhirConstants.ORGANIZATION);

        Identifier identifier = new Identifier();
        identifier.setUse(Identifier.IdentifierUse.OFFICIAL);
        identifier.setValue(healthCenterIdentifier);
        identifier.setSystem("https://hmis.health.go.ug/");

        reference.setDisplay(healthCenterName);
        reference.setIdentifier(identifier);
        return reference;
    }

    private List<Identifier> getEpisodeOfCareIdentifer(PatientProgram patientProgram) {
        List<Identifier> identifiers = new ArrayList<>();
        Identifier identifier = new Identifier();
        identifier.setUse(Identifier.IdentifierUse.USUAL);
        identifier.setValue(patientProgram.getUuid());
        identifier.setSystem("https://ugandaemr/");
        identifiers.add(identifier);

        return identifiers;
    }

    private EpisodeOfCare.EpisodeOfCareStatus getStatus(PatientProgram patientProgram) {
        if (patientProgram.getActive()) {
            return EpisodeOfCare.EpisodeOfCareStatus.ACTIVE;
        } else {
            return EpisodeOfCare.EpisodeOfCareStatus.FINISHED;
        }
    }
}
