/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.api.translators;


import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirUpdatableTranslator;

import javax.annotation.Nonnull;

public interface EpisodeOfCareTranslator<T> extends OpenmrsFhirUpdatableTranslator<T, EpisodeOfCare> {

    /**
     * Maps {@link org.openmrs.PatientProgram} to a {@link org.hl7.fhir.r4.model.EpisodeOfCare} resource
     *
     * @param patientProgram the OpenMRS patientProgram to translate
     * @return the corresponding FHIR EpisodeOfCare resource
     */
    @Override
    EpisodeOfCare toFhirResource(@Nonnull T patientProgram);
}
