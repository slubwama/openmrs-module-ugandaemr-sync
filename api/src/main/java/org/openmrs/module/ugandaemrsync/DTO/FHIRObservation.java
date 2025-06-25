package org.openmrs.module.ugandaemrsync.DTO;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.hl7.fhir.r4.model.Observation;

@JsonRootName("Observation")
public class FHIRObservation extends Observation {

}
