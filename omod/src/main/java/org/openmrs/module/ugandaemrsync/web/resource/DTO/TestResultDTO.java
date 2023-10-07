package org.openmrs.module.ugandaemrsync.web.resource.DTO;

import org.openmrs.Encounter;
import org.openmrs.Patient;

import java.io.Serializable;
import java.util.List;

public class TestResultDTO implements Serializable {
    private Patient patient;

    private List<Encounter> encounterList;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<Encounter> getEncounterList() {
        return encounterList;
    }

    public void setEncounterList(List<Encounter> encounterList) {
        this.encounterList = encounterList;
    }
}
