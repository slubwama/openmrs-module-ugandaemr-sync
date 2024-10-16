package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.TestOrder;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.api.impl.UgandaEMRSyncServiceImpl;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.*;

/**
 * Posts Viral load PROGRAM data to the central server
 */

public class SendViralLoadProgramDataToCentralServerTask extends AbstractTask {

    protected Log log = LogFactory.getLog(SendViralLoadProgramDataToCentralServerTask.class);

    @Override
    public void execute() {
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        List<Order> orderList = new ArrayList<>();

        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            return;
        }

        try {
            orderList = getOrders();
        } catch (IOException e) {
            log.error("Failed to get orders", e);
        } catch (ParseException e) {
            log.error("Failed to pass orders to list", e);
        }

        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VL_PROGRAM_DATA_SYNC_TYPE_UUID);
        SyncTaskType firstMessageSyncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);

        for (Order order : orderList) {
            List<SyncTask> allSyncTasks =  ugandaEMRSyncService.getSyncTasksBySyncTaskId(order.getAccessionNumber());
            List<SyncTask> successfullVLProgramSyncTasks = allSyncTasks.stream().filter(p -> syncTaskType.getId().equals(p.getSyncTaskType().getId()) && (p.getStatusCode()==200 || p.getStatusCode()==201)).collect(Collectors.toList());
            List<SyncTask> firstSyncTaskToRun = allSyncTasks.stream().filter(p -> firstMessageSyncTaskType.getId().equals(p.getSyncTaskType().getId())).collect(Collectors.toList());

            if (successfullVLProgramSyncTasks.size()<1 && firstSyncTaskToRun.size()>0) {


                try {
                    Map<String, String> dataOutput = generateVLProgramDataFHIRBody((TestOrder) order, VL_SEND_PROGRAM_DATA_FHIR_JSON_STRING);
                    String json = dataOutput.get("json");
                    String empty_fields = dataOutput.get("empty_fields");
                    String patientARTno = dataOutput.get("patient");
                    ugandaEMRSyncService.deleteSyncTask(order.getAccessionNumber(),syncTaskType);
                    Map map = ugandaEMRHttpURLConnection.sendPostBy(syncTaskType.getUrl(), syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword(), "", json, false);
                    if (map != null) {
                        SyncTask newSyncTask = new SyncTask();
                        newSyncTask.setDateSent(new Date());
                        newSyncTask.setCreator(Context.getUserService().getUser(1));
                        newSyncTask.setSentToUrl(syncTaskType.getUrl());
                        newSyncTask.setRequireAction(true);
                        newSyncTask.setActionCompleted(true);
                        newSyncTask.setSyncTask(order.getAccessionNumber());
                        newSyncTask.setStatusCode((Integer) map.get("responseCode"));
                        if(empty_fields!="") {
                            newSyncTask.setStatus((String) map.get("responseMessage") + " Patient "+ patientARTno+" empty fields: " + empty_fields);
                        }else{
                            newSyncTask.setStatus((String) map.get("responseMessage"));
                        }
                        newSyncTask.setSyncTaskType(ugandaEMRSyncService.getSyncTaskTypeByUUID(VL_PROGRAM_DATA_SYNC_TYPE_UUID));
                        ugandaEMRSyncService.saveSyncTask(newSyncTask);
                    }
                } catch (Exception e) {
                    log.error("Failed to create sync task",e);
                }
            }
        }
    }


    public Map<String, String> generateVLProgramDataFHIRBody(TestOrder testOrder, String jsonFHIRMap) {
        Map<String, String> jsonMap = new HashMap<>();
        UgandaEMRSyncService ugandaEMRSyncService = new UgandaEMRSyncServiceImpl();
        String filledJsonFile = "";
        String empty_fields="";
        if (testOrder != null) {
            AdministrationService administrationService = Context.getAdministrationService();

            String healthCenterCode = ugandaEMRSyncService.getHealthCenterCode();
            String otherID= "";
            Patient patient = testOrder.getPatient();
            int patientId = patient.getPatientId();
            Date date_activated = testOrder.getDateActivated();
            String patientARTNO = ugandaEMRSyncService.getPatientIdentifier(testOrder.getPatient(),PATIENT_IDENTIFIER_TYPE);
            String patientOpenMRSID = ugandaEMRSyncService.getPatientIdentifier(testOrder.getPatient(),OPENMRS_IDENTIFIER_TYPE_UUID);
            String patientANCID = ugandaEMRSyncService.getPatientIdentifier(testOrder.getPatient(),ANC_IDENTIFIER_TYPE_UUID);
            String patientNATIONALID = ugandaEMRSyncService.getPatientIdentifier(testOrder.getPatient(),NATIONAL_ID_IDENTIFIER_TYPE_UUID);
            String patientPNC_ID= ugandaEMRSyncService.getPatientIdentifier(testOrder.getPatient(),PNC_IDENTIFIER_TYPE_UUID);
            String sampleID = testOrder.getAccessionNumber();
            String gender = patient.getGender();
            List current_regimenList = administrationService.executeSQL(String.format(Latest_obs_of_Person,"value_coded", patientId,90315,date_activated),true);
            List current_regimen_orders_List = administrationService.executeSQL(String.format(Latest_drug_order_of_person, patientId,date_activated),true);

            String current_regimen="";
            int regimen_code=0;
            if(current_regimenList.size() > 0) {
                ArrayList regimenList = (ArrayList) current_regimenList.get(0);
                 regimen_code = Integer.parseInt(regimenList.get(0).toString());
                current_regimen = Context.getConceptService().getConcept(regimen_code).getName().getName();
            }else if (current_regimen_orders_List.size()>0){
                ArrayList regimenList = (ArrayList) current_regimen_orders_List.get(0);
                regimen_code = Integer.parseInt(regimenList.get(0).toString());

                current_regimen = Context.getConceptService().getConcept(regimen_code).getName().getName();
            }
            if(current_regimen.isEmpty())
                empty_fields += ", current regimen";

            List obs_dsdmList = administrationService.executeSQL(String.format(Latest_obs_of_Person,"value_coded", patientId,165143,date_activated),true);

            String dsdm="";
            String dsdm_hie_code="";
            if(obs_dsdmList.size()>0) {
                ArrayList myList = (ArrayList) obs_dsdmList.get(0);
                int dsdm_code = Integer.parseInt(myList.get(0).toString());


                if(dsdm_code==165138){
                    dsdm = "FBIM";
                    dsdm_hie_code = "734163000_01";
                }
                else if (dsdm_code==165140) {
                    dsdm = "FBG";
                    dsdm_hie_code = "734163000_02";
                }
                else if(dsdm_code==165139){
                    dsdm = "FTDR";
                    dsdm_hie_code = "734163000_03";
                }
                else if (dsdm_code==165142) {
                    dsdm = "CDDP";
                    dsdm_hie_code = "734163000_04";
                }
                else if(dsdm_code==165141){
                    dsdm = "CCLAD";
                    dsdm_hie_code = "734163000_05";
                }
            }
            if(dsdm_hie_code.isEmpty())
                dsdm_hie_code="734163000_01";

            List obs_adherenceList = administrationService.executeSQL(String.format(Latest_obs_of_Person,"value_coded", patientId,90221,date_activated),true);
            String adherence="";
            String adherence_hie_code ="";
            Integer adherence_code;
            if(obs_adherenceList.size()>0) {
                ArrayList myList = (ArrayList) obs_adherenceList.get(0);
                adherence_code =Integer.parseInt(myList.get(0).toString());
                if(adherence_code==90156){
                    adherence = "Good >= 95%";
                    adherence_hie_code = "1156699004_01";
                } else if (adherence_code==90157) {
                    adherence = "Fair 85-94%";
                    adherence_hie_code = "1156699004_02";
                }else if(adherence_code==90158){
                    adherence = "Poor <85%";
                    adherence_hie_code = "1156699004_03";
                }
            }
            if(adherence_hie_code.isEmpty())
                empty_fields += ", adherence";

            List current_regimen_start_date = administrationService.executeSQL(String.format("SELECT TIMESTAMPDIFF(MONTH, obs_datetime,'%s') from obs where person_id=%s and concept_id=90315 and voided=0 and value_coded = %s ORDER BY obs_datetime ASC LIMIT 1",date_activated.toString(),patientId,regimen_code),true);
            List current_regimen_start_date_by_orders = administrationService.executeSQL(String.format("SELECT TIMESTAMPDIFF(MONTH, date_activated,'%s') from orders o INNER JOIN order_type ot ON o.order_type_id = ot.order_type_id\n" +
                    "\t INNER JOIN drug_order d_o ON o.order_id = d_o.order_id\n" +
                    "\t where ot.uuid='131168f4-15f5-102d-96e4-000c29c2a5d7' and patient_id= %s  and o.voided=0 and o.concept_id = %s ORDER BY date_activated ASC LIMIT 1",date_activated.toString(),patientId,regimen_code),true);

            String duration_string="";
            String duration_string_hie_code="";
            if(current_regimen_start_date.size()>0) {
                ArrayList myList = (ArrayList) current_regimen_start_date.get(0);
                int duration= Integer.parseInt(myList.get(0).toString());
                 if(duration >=60) {
                     duration_string=">5yrs";
                     duration_string_hie_code = "261773006_05";
                 }
                 else if(duration >=24 && duration < 60 ) {
                     duration_string="2 -< 5yrs";
                     duration_string_hie_code = "261773006_04";
                 }
                 else if(duration >=12 && duration < 24 ) {
                     duration_string="1 - 2yrs";
                     duration_string_hie_code = "261773006_03";
                 }
                 else if(duration >=6 && duration < 12) {
                     duration_string="6 months - < 1yr";
                     duration_string_hie_code = "261773006_02";
                 }
                 else if( duration < 6) {
                     duration_string="< 6months";
                     duration_string_hie_code = "261773006_01";
                 }
            } else if (current_regimen_start_date_by_orders.size()>0) {
                ArrayList myList = (ArrayList) current_regimen_start_date_by_orders.get(0);
                int duration= Integer.parseInt(myList.get(0).toString());
                if(duration >=60) {
                    duration_string=">5yrs";
                    duration_string_hie_code = "261773006_05";
                }
                else if(duration >=24 && duration < 60 ) {
                    duration_string="2 -< 5yrs";
                    duration_string_hie_code = "261773006_04";
                }
                else if(duration >=12 && duration < 24 ) {
                    duration_string="1 - 2yrs";
                    duration_string_hie_code = "261773006_03";
                }
                else if(duration >=6 && duration < 12) {
                    duration_string="6 months - < 1yr";
                    duration_string_hie_code = "261773006_02";
                }
                else if( duration < 6) {
                    duration_string="< 6months";
                    duration_string_hie_code = "261773006_01";
                }
            }
            if (duration_string_hie_code.isEmpty())
                empty_fields += ", duration on art";

            List obs_pregnantList = administrationService.executeSQL(String.format(Latest_obs_of_Person,"value_coded", patientId,90041,date_activated),true);

            Boolean pregnant=false;
            Boolean breastfeeding=false;
            if(obs_pregnantList.size()>0){
                ArrayList myList = (ArrayList) obs_pregnantList.get(0);
                int preg_status = Integer.parseInt(myList.get(0).toString());
              if(preg_status==1065){
                  pregnant= true;
              }else if(preg_status==99601){
                  breastfeeding = true;
              }
            }

            List obs_tbList = administrationService.executeSQL(String.format(Latest_obs_of_Person,"value_coded", patientId,90216,date_activated),true);

            Boolean hasActiveTB = false;
            String tb_phase="";
            String tb_phase_hie_code="";
            List<Integer> diagnosed_concepts = Arrays.asList(165295, 165296, 165297, 165298, 165299,165300);
            if(obs_tbList.size()>0){
                ArrayList myList = (ArrayList) obs_tbList.get(0);
                int tb_status_answer =Integer.parseInt(myList.get(0).toString());
              if(tb_status_answer==90071){
                  tb_phase= "Continuation Phase";
                  tb_phase_hie_code ="371569005_02";
                  hasActiveTB =true;
              }else if (diagnosed_concepts.contains(tb_status_answer) ){
                  tb_phase= "Initiation Phase";
                  tb_phase_hie_code ="371569005_01";
                  hasActiveTB=true;
              }
            }

            List artStartList = administrationService.executeSQL(String.format(Latest_obs_of_Person,"DATE(value_datetime)", patientId,99161,date_activated),true);
            Date artStartDate = null;
            if(artStartList.size()>0){
                ArrayList myList = (ArrayList) artStartList.get(0);
                artStartDate = Context.getService(UgandaEMRSyncService.class).convertStringToDate(myList.get(0).toString(),"","yyyy-MM-dd");
            }

            if(artStartDate==null)
                empty_fields += ", ART Start Date";

            List obs_indication_for_VL = administrationService.executeSQL(String.format(Latest_obs_of_Person,"value_coded", patientId,168689,date_activated),true);

            String vl_indication_hie_code ="";
            int vl_indicator_code=0;
            if(obs_indication_for_VL.size()>0) {
                ArrayList myList = (ArrayList) obs_indication_for_VL.get(0);
                 vl_indicator_code = Integer.parseInt(myList.get(0).toString());

                vl_indication_hie_code = getVl_indication_hie_code(vl_indicator_code);
            }
            if(vl_indication_hie_code.isEmpty()){
//                do manual fill of vl indication warning : this is a hack
                List last_vl = administrationService.executeSQL(String.format(Latest_obs_of_Person,"DATE(value_datetime)", patientId,163023,date_activated),true);
                if(pregnant){
                        vl_indicator_code= 166508;
                }else if(artStartDate!=null && isSixToSevenMonthsAgo(artStartDate,date_activated)){
                        vl_indicator_code=168683;
                }else if( artStartDate!=null &&isTwelveToThirteenMonthsAgo(artStartDate,date_activated)){
                        vl_indicator_code = 168684;
                }else if(last_vl.size()>0){
                        ArrayList myList = (ArrayList) last_vl.get(0);
                        Date  last_vl_date = Context.getService(UgandaEMRSyncService.class).convertStringToDate(myList.get(0).toString(),"","yyyy-MM-dd");

                        if(patient.getAge() <= 15 && last_vl_date!=null && isSixToSevenMonthsAgo(last_vl_date,date_activated)){
                            vl_indicator_code=168688 ;
                        }else if(patient.getAge() >15 && last_vl_date!=null && isTwelveToThirteenMonthsAgo(last_vl_date,date_activated)){
                            vl_indicator_code=168688 ;
                        }else{
                            vl_indicator_code=168688 ;
                        }
                }else{
                    vl_indicator_code=168688 ;
                }
                vl_indication_hie_code = getVl_indication_hie_code(vl_indicator_code);
                if(vl_indication_hie_code.isEmpty()){
                    empty_fields +=  " Indication for viral load";
                }
            }

            List obs_WHOList = administrationService.executeSQL(String.format(Latest_obs_of_Person,"value_coded", patientId,90203,date_activated),true);

            String who_hie_code = "";
            String who_display = "";
            if(obs_WHOList.size()>0){
                ArrayList myList = (ArrayList) obs_WHOList.get(0);
                int who_stage_concept =(Integer)myList.get(0);

                switch (who_stage_concept) {
                    case 90033:
                        //1
                        who_hie_code="737378009";
                        who_display="WHO 2007 HIV infection clinical stage 1";
                        break;
                    case 90034:
                        //2
                        who_hie_code="737379001";
                        who_display="WHO 2007 HIV infection clinical stage 2";
                        break;
                    case 90035:
                        //3
                        who_hie_code="737380003";
                        who_display="WHO 2007 HIV infection clinical stage 3";
                        break;
                    case 90036:
                        //4
                        who_hie_code="737381004";
                        who_display="WHO 2007 HIV infection clinical stage 4";
                        break;
                    case 90293:
                        //T1
                        who_hie_code="737378009";
                        who_display="WHO 2007 HIV infection clinical stage 1";
                        break;
                    case 90294:
                        //T2
                        who_hie_code="737379001";
                        who_display="WHO 2007 HIV infection clinical stage 2";
                        break;
                    case 90295:
                        //T3
                        who_hie_code="737380003";
                        who_display="WHO 2007 HIV infection clinical stage 3";
                        break;
                    case 90296:
                        //T4
                        who_hie_code="737381004";
                        who_display="WHO 2007 HIV infection clinical stage 4";
                        break;
                    default:
                        who_hie_code="737378009";
                        who_display="WHO 2007 HIV infection clinical stage 1";
                        break;
                }
            }
            if(who_hie_code.isEmpty())
                empty_fields += ", who code";

            String regimenline = getRegimenLineOfPatient(patient);
            String coded_regimen_line ="";
            if(regimenline=="first"){
               coded_regimen_line= firstLineBody;
            }else if (regimenline=="second"){
               coded_regimen_line=secondLineBody;
            }else if(regimenline=="third"){
               coded_regimen_line=thirdLineBody;
            }
            if(coded_regimen_line.isEmpty())
                coded_regimen_line= firstLineBody;

            jsonMap.put("empty_fields",empty_fields);
            jsonMap.put("patient",patientARTNO);

            filledJsonFile = String.format(jsonFHIRMap,patientARTNO,sampleID,patientARTNO,patientOpenMRSID,patientNATIONALID,patientANCID,otherID,patientPNC_ID,gender, patient.getBirthdate(),healthCenterCode,patient.getAge(),artStartDate,who_hie_code,who_display,duration_string_hie_code,pregnant,breastfeeding,hasActiveTB,tb_phase_hie_code,adherence_hie_code,dsdm_hie_code,vl_indication_hie_code,coded_regimen_line,current_regimen);
        }
        jsonMap.put("json", filledJsonFile);
        return jsonMap;
    }

    private static String getVl_indication_hie_code(int vl_indicator_code) {

        String vl_indication_hie_code ="";

        if(vl_indicator_code ==168683){ // 6 months after ART initiation
            vl_indication_hie_code = "315124004_01";
        }
        else if (vl_indicator_code ==168684) { //12 months after ART initiation
            vl_indication_hie_code = "315124004_02";
        }
        else if(vl_indicator_code ==168688){ //Routine
            vl_indication_hie_code = "315124004_03";
        }
        else if (vl_indicator_code ==168687) { //Repeat (after IAC)
            vl_indication_hie_code = "315124004_04";
        }
        else if(vl_indicator_code ==168685){ //Suspected treatment failure
            vl_indication_hie_code = "315124004_05";
        }
        else if (vl_indicator_code ==166508) { //1st ANC visit
            vl_indication_hie_code = "315124004_06";
        }
        else if(vl_indicator_code ==168686){ //Special Considerations
            vl_indication_hie_code = "315124004_07";
        }
        return vl_indication_hie_code;
    }


    public List<Order> getOrders() throws IOException, ParseException {
        OrderService orderService = Context.getOrderService();
        List<Order> orders = new ArrayList<>();
        List list = Context.getAdministrationService().executeSQL(VIRAL_LOAD_ORDERS_QUERY, true);
        if (list.size() > 0) {
            for (Object o : list) {
                Order order = orderService.getOrder(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString()));
                if (order.getAccessionNumber() != null && order.isActive() && order.getInstructions().equalsIgnoreCase("REFER TO cphl")) {
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    private String getRegimenLineOfPatient(Patient patient){
        int patientno = patient.getPatientId();
        String line="";
        String firstLineQuery = String.format(REGIMEN_LINE_QUERY,"ab6d1f1d-fcf6-4255-8b6f-2bf8959ad8f2",patientno);

        List firstLineResults = Context.getAdministrationService().executeSQL(firstLineQuery,true);
        if(firstLineResults.size()>0 ){
            line ="first";
        }else {
            String secondLineQuery = String.format(REGIMEN_LINE_QUERY, "9a42a3ad-d8a4-4f2e-9fa0-04d5f2e6436e", patientno);
            List secondLineResults = Context.getAdministrationService().executeSQL(secondLineQuery, true);
            if (secondLineResults.size() > 0) {
                line = "second";
            }

            String thirdLineQuery = String.format(REGIMEN_LINE_QUERY,"5d2d0e7e-69a6-408a-b5ce-8d93fb72bc21",patientno);
            List thirdLineResults = Context.getAdministrationService().executeSQL(thirdLineQuery,true);
            if(thirdLineResults.size()>0 ){
                line ="third";
            }
        }

        return line;
    }


    public boolean isSixToSevenMonthsAgo(Date date,Date dateActivated) {
        // Convert Date to LocalDate
        LocalDate inputDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Get current date
        LocalDate activeDate = dateActivated.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Calculate the date 6 and 7 months ago
        LocalDate sixMonthsAgo = activeDate.minusMonths(6);
        LocalDate sevenMonthsAgo = activeDate.minusMonths(7);

        // Check if the input date is between 6 and 7 months ago
        return (inputDate.isBefore(sevenMonthsAgo) || inputDate.isEqual(sevenMonthsAgo)) &&
                (inputDate.isAfter(sixMonthsAgo) || inputDate.isEqual(sixMonthsAgo));
    }

    public boolean isTwelveToThirteenMonthsAgo(Date date,Date dateActivated) {
        LocalDate inputDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate activeDate = dateActivated.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate twelveMonthsAgo = activeDate.minusMonths(11);
        LocalDate thirteenMonthsAgo = activeDate.minusMonths(13);

        return (inputDate.isBefore(thirteenMonthsAgo) || inputDate.isEqual(thirteenMonthsAgo)) &&
                (inputDate.isAfter(twelveMonthsAgo) || inputDate.isEqual(twelveMonthsAgo));
    }

    public boolean is12MonthsAfter(Date date,Date dateActivated) {
        LocalDate inputDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate activeDate = dateActivated.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate thirteenMonthsAgo = activeDate.minusMonths(13);

        return (inputDate.isAfter(thirteenMonthsAgo) );
    }
}
