package org.openmrs.module.ugandaemrsync.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.HIV_ENCOUNTER_PAGE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_FACILITY_DHIS2_ID_CELL_NO;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_FACILITY_NAME_CELL_NO;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_DATE_COLLECTION_CELL_NO;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_PATIENT_ART_ID_CELL_NO;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_RESULTS_NUMERIC_CELL_NO;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_RESULTS_ALHPA_NUMERIC_CELL_NO;

public class ViralLoadUploadPageController {
    protected final Log log = LogFactory.getLog(ViralLoadUploadPageController.class);

    private List<String> noEncounterFound = new ArrayList<>();

    private List<String> noPatientFound = new ArrayList<>();

    private List<String> patientResultNotReleased = new ArrayList<>();

    private String healthCenterNameValidator = "";

    public void get(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride) {
        pageModel.put("breadcrumbOverride", breadcrumbOverride);
        pageModel.put("noEncounterFound", this.noEncounterFound);
        pageModel.put("noPatientFound", this.noPatientFound);
        pageModel.put("patientResultNotReleased", this.patientResultNotReleased);
        pageModel.put("healthCenterNameValidator", this.healthCenterNameValidator);
    }

    public void post(@SpringBean PageModel pageModel, UiUtils ui, @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride, @RequestParam(value = "returnUrl", required = false) String returnUrl, @RequestParam(value = "file", required = false) MultipartFile file) {

        readCSVFile(file);
        pageModel.put("breadcrumbOverride", breadcrumbOverride);
        pageModel.put("noEncounterFound", this.noEncounterFound);
        pageModel.put("noPatientFound", this.noPatientFound);
        pageModel.put("patientResultNotReleased", this.patientResultNotReleased);
        pageModel.put("healthCenterNameValidator", this.healthCenterNameValidator);
    }

    private void readCSVFile(MultipartFile csvFile) {

        UgandaEMRSyncService ugandaEMRSyncService=Context.getService(UgandaEMRSyncService.class);

        List<Patient> noEncounterFound = new ArrayList<>();
        List<String> noPatientFound = new ArrayList<>();
        List<Patient> patientResultNotReleased = new ArrayList<>();
        String cvsSplitBy = ",";
        InputStream is = null;
        try {
            is = csvFile.getInputStream();
        } catch (IOException e) {
            log.error("An error occued during the reading of the file uploaded",e);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            Object[] objects = br.lines().toArray();
            Collection<EncounterType> encounterTypes = ugandaEMRSyncService.getEcounterTypes(HIV_ENCOUNTER_PAGE_UUID);


            if (objects.length >= 1) {
                if (ugandaEMRSyncService.validateFacility(objects[1].toString().split(cvsSplitBy)[VL_FACILITY_DHIS2_ID_CELL_NO].replaceAll("\"", ""))) {
                    healthCenterNameValidator="Invalid Health Center DHIS2 UUID";

                } else {
                    healthCenterNameValidator = objects[1].toString().split(cvsSplitBy)[VL_FACILITY_NAME_CELL_NO].replaceAll("\"", "");

                    for (int i = 0; i < objects.length; i++) {
                        // use comma as separator
                        String[] vlResult = objects[i].toString().split(cvsSplitBy);


                        //When on headers of the CSV File
                        if (vlResult[VL_DATE_COLLECTION_CELL_NO].contentEquals("date_collected")) continue;

                        String vlDate = vlResult[VL_DATE_COLLECTION_CELL_NO].replaceAll("\"", "");
                        String patientARTNo = vlResult[VL_PATIENT_ART_ID_CELL_NO].replaceAll("\"", "");
                        String vlQuantitative = vlResult[VL_RESULTS_NUMERIC_CELL_NO].replaceAll("\"", "");
                        String vlQualitative = vlResult[VL_RESULTS_ALHPA_NUMERIC_CELL_NO].replaceAll("\"", "");
                        String dateFormat = ugandaEMRSyncService.getDateFormat(vlDate);


                        EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteria(ugandaEMRSyncService.getPatientByPatientIdentifier(patientARTNo), null, ugandaEMRSyncService.convertStringToDate(vlDate, "00:00:00", dateFormat), ugandaEMRSyncService.convertStringToDate(vlDate, "23:59:59", dateFormat), null, null, encounterTypes, null, null, null, false);
                        List<Encounter> encounters = new ArrayList<>();
                        //Determine if patient is found
                        if (encounterSearchCriteria.getPatient() != null) {
                            encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);

                            if (encounters.size() > 0) {
                                try {
                                    ugandaEMRSyncService.addVLToEncounter(vlQualitative, vlQuantitative, vlDate, encounters.get(0),null);
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    log.error("Failed to add viral load ro encounter", e);
                                }
                            } else {
                                this.noEncounterFound.add(vlResult[VL_PATIENT_ART_ID_CELL_NO]);
                            }
                        } else {
                            this.noPatientFound.add(vlResult[VL_PATIENT_ART_ID_CELL_NO]);
                        }
                    }

                }
            }

        } catch (IOException e) {
            log.error("Failed to read file", e);
        }

    }

}
