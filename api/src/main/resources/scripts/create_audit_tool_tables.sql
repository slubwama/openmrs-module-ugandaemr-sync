DROP TABLE IF EXISTS reporting_audit_tool_hiv;

CREATE TABLE reporting_audit_tool_hiv (
    ID int(11) NOT NULL AUTO_INCREMENT,
    PATTIENT_NO INT(11) NOT NULL ,
    ART_NO varchar(80) DEFAULT NULL,
    Gender varchar(80) DEFAULT NULL,
    Date_of_Birth datetime DEFAULT NULL,
    Age int(6) DEFAULT NULL,
    Pregnant_Status varchar(80) DEFAULT NULL,
    Weight INT(8) DEFAULT NULL ,
    DSDM varchar(80) DEFAULT NULL,
    Visit_type varchar(80) DEFAULT NULL,
    Last_Visit_Date datetime DEFAULT NULL,
    Next_Appointment_Date datetime DEFAULT NULL,
    Prescription_Duration int(6) DEFAULT NULL,
    Art_Start_Date datetime DEFAULT NULL,
    Adherence varchar(80) DEFAULT NULL,
    Current_regimen varchar(80) DEFAULT NULL,
    VL_Quantitative int(6) DEFAULT NULL,
    Last_TPT_Status varchar(80) DEFAULT NULL,
    TB_Status varchar(80) DEFAULT NULL,
    HEP_B_Status varchar(80) DEFAULT NULL,
    Sphillis_Status varchar(80) DEFAULT NULL,
    Family_Planning varchar(64) DEFAULT NULL,
    Advanced_Disease varchar(80) DEFAULT NULL,
    VL_Date date DEFAULT NULL,
    CHILD_AGE int(6) DEFAULT NULL,
    CHILD_KNOWN int(6) DEFAULT NULL,
    CHILD_POSITIVE int(6) DEFAULT NULL,
    CHILD_ON_ART int(6) DEFAULT NULL,
    PARTNER_AGE int(6) DEFAULT NULL,
    PARTNER_KNOWN int(6) DEFAULT NULL,
    PARTNER_POSITIVE int(6) DEFAULT NULL,
    PARTNER_ONART int(6) DEFAULT NULL,
    PSY_CODES varchar(80) DEFAULT NULL,
    DEPRESSION varchar(80) DEFAULT NULL,
    GBV varchar(80) DEFAULT NULL,
    LINKAGE varchar(80) DEFAULT NULL,
    OVC_SCREENING  varchar(80) DEFAULT NULL,
    OVC_ENROLLMENT  varchar(80) DEFAULT NULL,
    NUTRITION_STATUS varchar(80) DEFAULT NULL,
    NUTRITION_SUPPORT varchar(80) DEFAULT NULL,
    CACX_STATUS  varchar(80) DEFAULT NULL,
    STABLE varchar(80) DEFAULT NULL,
    REGIMEN_LINE int(6) DEFAULT NULL,
    PRIORITY_POPULATION varchar(80) DEFAULT NULL,
    DEAD int(6) DEFAULT NULL,
    TRANSFER_OUT varchar(80) DEFAULT NULL,
    DAYS_LOST varchar(50) DEFAULT NULL,
    DURATION_ON_ART varchar(50) DEFAULT NULL,
    IAC int(6) DEFAULT NULL,
    HIVDR_date varchar(80) DEFAULT NULL,
    HIVDR_SAMPLE_COLLECTED varchar(80) DEFAULT NULL,
    SWITCHED varchar(80) DEFAULT NULL,
    NEW_BLED_DATE varchar(80) DEFAULT NULL,
    HEALTH_EDUC_DATE varchar(80) DEFAULT NULL,
    PSS_ISSUES varchar(80) DEFAULT NULL,
    PSS_INTERVENTION varchar(80) DEFAULT NULL,
    PRIMARY KEY (ID)


) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO reporting_audit_tool_hiv (
    ART_NO ,
    PATTIENT_NO,
    Gender,
    Date_of_Birth,
    Age ,
    Pregnant_Status,
    Weight,
    DSDM ,
    Visit_type,
    Last_Visit_Date ,
    Next_Appointment_Date ,
    Prescription_Duration ,
    Art_Start_Date ,
    Adherence ,
    Current_regimen ,
    VL_Quantitative ,
    Last_TPT_Status ,
    TB_Status,
    HEP_B_Status,
    Sphillis_Status,
    Family_Planning,
    Advanced_Disease,
    VL_Date ,
    CHILD_AGE ,
    CHILD_KNOWN ,
    CHILD_POSITIVE,
    CHILD_ON_ART,
    PARTNER_AGE ,
    PARTNER_KNOWN ,
    PARTNER_POSITIVE,
    PARTNER_ONART,
    PSY_CODES ,
    DEPRESSION,
    GBV,
    LINKAGE,
    OVC_SCREENING ,
    OVC_ENROLLMENT ,
    NUTRITION_STATUS,
    NUTRITION_SUPPORT,
    CACX_STATUS ,
    STABLE,
    REGIMEN_LINE,
    PRIORITY_POPULATION,
    DEAD,
    TRANSFER_OUT,
    DAYS_LOST,
    DURATION_ON_ART,
    IAC,
    HIVDR_date,
    HIVDR_SAMPLE_COLLECTED,
    SWITCHED,
    NEW_BLED_DATE,
    HEALTH_EDUC_DATE,
    PSS_ISSUES,
    PSS_INTERVENTION )
    SELECT identifier, patient, gender , p.birthdate, TIMESTAMPDIFF(YEAR, p.birthdate, CURRENT_DATE()) as age,
     IF(Preg.name = 'Yes', 'Pregnant', IF(Preg.name = 'NO', 'Not Pregnant', Preg.name)) as pregnant_status,
     Wgt.value_numeric as Weight,
     IF(DSD.name = 'FTR', 'FTDR', DSD.name) as DSDM,
     vst_type.name as Visit_Type,
     last_enc.visit_date as Last_visit_date,
     returndate AS Next_Appointment_Date,
     MMD.value_numeric as NO_of_Days,
     ARTStartDate,
     IF(adherence.value_coded = 90156, 'Good (95%)',
        IF(adherence.value_coded = 90157, 'Fair (85-94%)',
           IF(adherence.value_coded = 90158, 'Poor (<85%)', adherence.value_coded))) as Adherence,
     current_regimen.name as Current_Regimen,
     VL.value_numeric as VL_copies,
     TPT.name as TPT_Status,
     TB.name as TB_Status,
     IF(HEPB.value_coded = 90001, 'Not Tested',
        IF(HEPB.value_coded = 159971, 'Tested', '')) as HEP_B_Status,
     SYPHILLIS.name as Sphillis_Status,
     family.name as Family_Planning,
     ADV_DZZ.name as Advanced_Disease,
     vl_bled.vl_date,
     INDEX_TESTING_CHILD_AGE.no,
     RELATIONSHIP_CHILD_STATUS.no,
     RELATIONSHIP_CHILD_POSITIVE.no,
     RELATIONSHIP_CHILD_ONART.no,
     INDEX_TESTING_PARTNER.no,
     RELATIONSHIP_PARTNER_STATUS.no,
     RELATIONSHIP_PARTNER_POSITIVE.no,
     RELATIONSHIP_PARTNER_ONART.no,
     psy_codes.name as CODES,
     DEPRESSION.name as depression,
     GBV.name,
     LINKAGE.name,
     OVC_SCREENING.name,
     OVC_ENROL.name,
     NUTRITION_STATUS.name,
     NUTRITION_SUPPORT.name,
     CACX_STATUS.name,
     IFNULL(STABLE.name, '') AS stable,
     IF(REGIMEN_LINES.concept_id = 90271, 1,
         IF(REGIMEN_LINES.concept_id = 90305,2,
 IF(REGIMEN_LINES.concept_id = 162987,3,1))),
     IFNULL(PP.name, '') as PP,
     p.dead,
     IFNULL(TOD.TOdate, ''),
     IFNULL(TIMESTAMPDIFF(DAY, DATE(returndate), DATE(CURRENT_DATE())), ''),
     IFNULL(TIMESTAMPDIFF(MONTH, DATE(ARTStartDate), DATE(returndate)), ''),
     IF(VL.value_numeric >= 1000, IFNULL(IAC.SESSIONS, 0), NULL),
     IF(VL.value_numeric >= 1000, IFNULL(HIVDRTEST.HIVDR_date, ''), ''),
     HIVDR_TEST_COLECTED.name as SAMPLE_COLLECTED,
     IF(VL.value_numeric >= 1000, IF(SWITCHED.value_coded in (163162, 163164), 'Y', 'N'), ''),
     IF(bled_for_vl.bled_date > vl_bled.vl_date, bled_for_vl.bled_date, '') as new_bled_date,
     HD_enc.visit_date,
     psy_issues.name,
     psy_intervention.name FROM  (select DISTINCT e.patient_id as patient from encounter e INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid in('8d5b27bc-c2cc-11de-8d13-0010c6dffd0f','8d5b2be0-c2cc-11de-8d13-0010c6dffd0f') and encounter_datetime<= CURRENT_DATE() and encounter_datetime>= DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH))cohort join
    person p on p.person_id = cohort.patient LEFT JOIN
    (SELECT pi.patient_id as patientid,identifier FROM patient_identifier pi INNER JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id and pit.uuid='e1731641-30ab-102d-86b0-7a5022ba4115'  WHERE  pi.voided=0 group by pi.patient_id)ids on patient=patientid
    LEFT JOIN(SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90041 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=90041 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)Preg on patient=Preg.person_id
       LEFT JOIN(SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=5089 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=5089 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <=CURRENT_DATE() group by o.person_id)Wgt on patient=Wgt.person_id
       LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165143 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='SHORT' and cn.locale='en'
    where o.concept_id=165143 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <=CURRENT_DATE() group by o.person_id)DSD on patient =DSD.person_id
       LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=160288 and voided=0 and value_coded in (164969,165284) group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=160288 and value_coded in (164969,165284) and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id) vst_type on patient= vst_type.person_id
       LEFT JOIN (select e.patient_id,max(DATE(encounter_datetime))visit_date from encounter e INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid in ('8d5b2be0-c2cc-11de-8d13-0010c6dffd0f','8d5b27bc-c2cc-11de-8d13-0010c6dffd0f') and encounter_datetime<= CURRENT_DATE() group by patient_id)as last_enc on patient=last_enc.patient_id
       LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as returndate FROM obs WHERE concept_id=5096 and voided=0 AND obs_datetime <=CURRENT_DATE() group by person_id)return_date on patient=return_date.person_id
       LEFT JOIN (SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99036 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=99036 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)MMD on patient=MMD.person_id
       LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as ARTStartDate FROM obs WHERE concept_id=99161 and voided=0 and  value_datetime<=CURRENT_DATE() AND obs_datetime <=CURRENT_DATE() group by person_id)ARTStart on patient=ARTStart.person_id
       LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90315 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=90315 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id) current_regimen on patient= current_regimen.person_id
       LEFT JOIN (SELECT o.person_id,value_coded from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90221 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=90221 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id) adherence on patient= adherence.person_id
       LEFT JOIN (SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=856 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=856 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)VL on patient=VL.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165288 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165288 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)TPT on patient= TPT.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90216 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=90216 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)TB on patient= TB.person_id
    LEFT JOIN (SELECT o.person_id,value_coded from obs o inner join (SELECT person_id,max(obs_datetime )latest_date from obs where concept_id=1322 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=1322 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)HEPB on patient= HEPB.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99753 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=99753 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)SYPHILLIS on patient= SYPHILLIS.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90238 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=90238 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)family on patient= family.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165272 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165272 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)ADV_DZZ on patient= ADV_DZZ.person_id
    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as vl_date FROM obs WHERE concept_id=163023 and voided=0 and  value_datetime<=CURRENT_DATE() AND obs_datetime <=CURRENT_DATE() group by person_id)vl_bled on patient=vl_bled.person_id
    LEFT JOIN (SELECT AGE.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded=90280
    )RELATIONSHIP INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=99074 and TIMESTAMPDIFF(YEAR,obs_datetime,CURRENT_DATE()) <=19
    )AGE on RELATIONSHIP.obs_group_id = AGE.obs_group_id group by AGE.person_id)INDEX_TESTING_CHILD_AGE on INDEX_TESTING_CHILD_AGE.person_id = patient
    LEFT JOIN (Select AGE.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded=90280
    )RELATIONSHIP INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id where concept_id=99074 and value_numeric <=19
    )AGE  on RELATIONSHIP.obs_group_id = AGE.obs_group_id INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=165275)C on C.obs_group_id= AGE.obs_group_id group by AGE.person_id
    )RELATIONSHIP_CHILD_STATUS on RELATIONSHIP_CHILD_STATUS.person_id = patient
    LEFT JOIN (Select AGE.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded=90280
    )RELATIONSHIP INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id where concept_id=99074 and value_numeric <=19
    )AGE  on RELATIONSHIP.obs_group_id = AGE.obs_group_id INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=165275 AND value_coded=90166)C on C.obs_group_id= AGE.obs_group_id group by AGE.person_id
    )RELATIONSHIP_CHILD_POSITIVE on RELATIONSHIP_CHILD_POSITIVE.person_id = patient
    LEFT JOIN (Select AGE.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded=90280
    )RELATIONSHIP INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id where concept_id=99074 and value_numeric <=19
    )AGE  on RELATIONSHIP.obs_group_id = AGE.obs_group_id INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=90270 AND value_coded=90003)C on C.obs_group_id= AGE.obs_group_id group by AGE.person_id
    )RELATIONSHIP_CHILD_ONART on RELATIONSHIP_CHILD_ONART.person_id = patient
    LEFT JOIN (Select RELATIONSHIP.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded in (90288,165274)
    )RELATIONSHIP  group by RELATIONSHIP.person_id)INDEX_TESTING_PARTNER on INDEX_TESTING_PARTNER.person_id = patient
    LEFT JOIN (Select RELATIONSHIP.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded in (90288,165274)
    )RELATIONSHIP INNER JOIN(SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=165275)C on C.obs_group_id= RELATIONSHIP.obs_group_id group by RELATIONSHIP.person_id
    )RELATIONSHIP_PARTNER_STATUS on RELATIONSHIP_PARTNER_STATUS.person_id = patient
    LEFT JOIN (Select RELATIONSHIP.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded in (90288,165274)
    )RELATIONSHIP INNER JOIN  (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=165275 AND value_coded=90166)C on C.obs_group_id= RELATIONSHIP.obs_group_id group by RELATIONSHIP.person_id
    )RELATIONSHIP_PARTNER_POSITIVE on RELATIONSHIP_PARTNER_POSITIVE.person_id = patient
    LEFT JOIN (Select RELATIONSHIP.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded=90280
    )RELATIONSHIP INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id
    where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=90270 AND value_coded=90003)C on C.obs_group_id= RELATIONSHIP.obs_group_id group by RELATIONSHIP.person_id
    )RELATIONSHIP_PARTNER_ONART on RELATIONSHIP_PARTNER_ONART.person_id = patient
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165185 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165185 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)psy_codes on patient= psy_codes.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165194 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165194 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)DEPRESSION on patient= DEPRESSION.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165302 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165302 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)GBV on patient= GBV.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165193 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165193 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)LINKAGE on patient= LINKAGE.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165200 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165200 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)OVC_SCREENING on patient= OVC_SCREENING.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165212 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165212 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)OVC_ENROL on patient= OVC_ENROL.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165050 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165050 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)NUTRITION_STATUS on patient= NUTRITION_STATUS.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99054 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=99054 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)NUTRITION_SUPPORT on patient= NUTRITION_SUPPORT.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165315 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165315 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)CACX_STATUS on patient= CACX_STATUS.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165144 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165144 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <=CURRENT_DATE() group by o.person_id)STABLE on patient =STABLE.person_id
    LEFT JOIN (SELECT pp.patient_id, program_workflow_state.concept_id from patient_state inner join program_workflow_state on patient_state.state = program_workflow_state.program_workflow_state_id
     inner join program_workflow on program_workflow_state.program_workflow_id = program_workflow.program_workflow_id inner join program on program_workflow.program_id = program.program_id inner join patient_program pp
         on patient_state.patient_program_id = pp.patient_program_id and program_workflow.concept_id=166214 and patient_state.end_date is null)REGIMEN_LINES ON patient = REGIMEN_LINES.patient_id
     LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id= 165169 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165169 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <=CURRENT_DATE() group by o.person_id)PP on patient =PP.person_id
    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as TOdate FROM obs WHERE concept_id=99165 and voided=0 and  value_datetime<=CURRENT_DATE() AND obs_datetime <=CURRENT_DATE() group by person_id)TOD on patient=TOD.person_id
    LEFT JOIN (select obs.person_id,count(value_datetime) SESSIONS from obs inner join (SELECT person_id, max(DATE (value_datetime))as vldate FROM obs WHERE concept_id=163023 and voided=0 and  value_datetime<=CURRENT_DATE() AND obs_datetime <=CURRENT_DATE() group by person_id
    )vl_date on vl_date.person_id= obs.person_id where concept_id=163154 and value_datetime>=vldate and obs_datetime between DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) and CURRENT_DATE() GROUP BY obs.person_id)IAC on patient =IAC.person_id
    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as HIVDR_date FROM obs WHERE concept_id=164989 and voided=0 AND obs_datetime <=CURRENT_DATE() group by person_id) HIVDRTEST on patient = HIVDRTEST.person_id
     LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=164989 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='SHORT' and cn.locale='en'
    where o.concept_id=164989 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <=CURRENT_DATE() group by o.person_id)HIVDR_TEST_COLECTED ON patient=HIVDR_TEST_COLECTED.person_id
    LEFT JOIN (SELECT o.person_id, value_coded from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=163166 and voided=0 group by person_id)A on o.person_id = A.person_id
     where o.concept_id=163166 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)SWITCHED on patient = SWITCHED.person_id
    LEFT JOIN (SELECT person_id,DATE(max(obs_datetime))bled_date from obs where concept_id=165845 and voided=0 group by person_id)bled_for_vl on patient=bled_for_vl.person_id
    LEFT JOIN (select e.patient_id,max(DATE(encounter_datetime))visit_date from encounter e INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid in ('6d88e370-f2ba-476b-bf1b-d8eaf3b1b67e') and encounter_datetime<= CURRENT_DATE() group by patient_id)as HD_enc on patient=HD_enc.patient_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165244 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
     where o.concept_id=165244 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)psy_issues on patient= psy_issues.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165190 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165190 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by o.person_id)psy_intervention on patient= psy_intervention.person_id;


DROP TABLE IF EXISTS reporting_audit_tool_non_suppressed;

CREATE TABLE reporting_audit_tool_non_suppressed (
    ID int(11) NOT NULL AUTO_INCREMENT,
    PATTIENT_NO INT(11) NOT NULL ,
    VL_REPEAT varchar(80) DEFAULT NULL,
    HIVDR_SAMPLE_COLLECTED varchar(80) DEFAULT NULL,
    VL_AFTER_IAC varchar(80) DEFAULT NULL,
    VL_COPIES varchar(80) DEFAULT NULL,
    RESULTS_RECEIVED varchar(80) DEFAULT NULL,
    HIVDR_RESULTS varchar(80) DEFAULT NULL,
    HIVDR_RESULTS_DATE varchar(80) DEFAULT NULL,
    DECISION_DATE varchar(80) DEFAULT NULL,
    DECISION_OUTCOME varchar(80) DEFAULT NULL,
    NEW_REGIMEN varchar(80) DEFAULT NULL,
     PRIMARY KEY (ID)


) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO reporting_audit_tool_non_suppressed (
    PATTIENT_NO,
    VL_REPEAT ,
    HIVDR_SAMPLE_COLLECTED ,
    VL_AFTER_IAC ,
    VL_COPIES ,
    RESULTS_RECEIVED ,
    HIVDR_RESULTS ,
    HIVDR_RESULTS_DATE ,
    DECISION_DATE ,
    DECISION_OUTCOME ,
    NEW_REGIMEN
)
SELECT patient,
       hivdr,
       hivdr_sample_collected.name,
       vl_after_iac.name,
       vl_copies.value_numeric,
       vlreceived.dates,
       hivdr_results.name,
       hivdr_result_date.dates,
       decision_date.dates,
       decision_outcome.name,
       new_regimen.name
FROM  (select DISTINCT e.patient_id as patient from encounter e INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid in('8d5b27bc-c2cc-11de-8d13-0010c6dffd0f','8d5b2be0-c2cc-11de-8d13-0010c6dffd0f') and encounter_datetime<= CURRENT_DATE() and encounter_datetime>= DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH))cohort join
    person p on p.person_id = cohort.patient INNER JOIN
    (SELECT o.person_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=856 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=856 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() and value_numeric>1000 group by o.person_id)VL on patient=VL.person_id
    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as hivdr FROM obs inner  join encounter e on obs.encounter_id=e.encounter_id INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id and et.uuid ='38cb2232-30fc-4b1f-8df1-47c795771ee9'  WHERE concept_id=163023 and obs.voided=0 and  value_datetime<=current_date() AND obs_datetime <=current_date() group by person_id)vlrepeat on patient=vlrepeat.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner  join encounter e on o.encounter_id=e.encounter_id INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id and et.uuid ='38cb2232-30fc-4b1f-8df1-47c795771ee9'  inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=164989 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=164989 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)hivdr_sample_collected on patient= hivdr_sample_collected.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner  join encounter e on o.encounter_id=e.encounter_id INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id and et.uuid ='38cb2232-30fc-4b1f-8df1-47c795771ee9' inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=1305 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=1305 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)vl_after_iac on patient= vl_after_iac.person_id
    LEFT JOIN(SELECT o.person_id,value_numeric from obs o inner  join encounter e on o.encounter_id=e.encounter_id INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id and et.uuid ='38cb2232-30fc-4b1f-8df1-47c795771ee9' inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=856 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=856 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <=current_date() group by o.person_id)vl_copies on patient=vl_copies.person_id
    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as dates FROM obs inner  join encounter e on obs.encounter_id=e.encounter_id INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id and et.uuid ='38cb2232-30fc-4b1f-8df1-47c795771ee9' WHERE concept_id=163150 and obs.voided=0 and  value_datetime<=current_date() AND obs_datetime <=current_date() group by person_id)vlreceived on patient=vlreceived.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner  join encounter e on o.encounter_id=e.encounter_id INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id and et.uuid ='38cb2232-30fc-4b1f-8df1-47c795771ee9' inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165824 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165824 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)hivdr_results on patient= hivdr_results.person_id
    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as dates FROM obs inner  join encounter e on obs.encounter_id=e.encounter_id INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id and et.uuid ='38cb2232-30fc-4b1f-8df1-47c795771ee9' WHERE concept_id=165823 and obs.voided=0 and  value_datetime<=current_date() AND obs_datetime <=current_date() group by person_id)hivdr_result_date on patient=hivdr_result_date.person_id
    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as dates FROM obs inner  join encounter e on obs.encounter_id=e.encounter_id INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id and et.uuid ='38cb2232-30fc-4b1f-8df1-47c795771ee9' WHERE concept_id=163167 and obs.voided=0 and  value_datetime<=current_date() AND obs_datetime <=current_date() group by person_id)decision_date on patient=decision_date.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner  join encounter e on o.encounter_id=e.encounter_id INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id and et.uuid ='38cb2232-30fc-4b1f-8df1-47c795771ee9' inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=163166 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=163166 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)decision_outcome on patient= decision_outcome.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner  join encounter e on o.encounter_id=e.encounter_id INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id and et.uuid ='38cb2232-30fc-4b1f-8df1-47c795771ee9' inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90315 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=90315 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)new_regimen on patient= new_regimen.person_id ;

DROP TABLE IF EXISTS reporting_audit_tool_hiv1;

CREATE TABLE reporting_audit_tool_hiv1 (
   ID int(11) NOT NULL AUTO_INCREMENT,
   PATTIENT_NO INT(11) NOT NULL ,
   HIV_ENROLLMENT_DATE varchar(80) DEFAULT NULL,
   TEST_TYPE varchar(80) DEFAULT NULL,
   CARE_ENTRY_POINT varchar(80) DEFAULT NULL,
   TEMP varchar(80) DEFAULT NULL,
   RESPIRATORY_RATE varchar(80) DEFAULT NULL,
   HEART_RATE varchar(80) DEFAULT NULL,
   CLIENT_CATEGORY varchar(80) DEFAULT NULL,
   MARITAL_STATUS varchar(80) DEFAULT NULL,
   REGISTRATION_DATE varchar(80) DEFAULT NULL,
   SIGNS_AND_SYMPTOMS varchar(80) DEFAULT NULL,
   SIDE_EFFECTS varchar(80) DEFAULT NULL,
   PSS4 varchar(80) DEFAULT NULL,
   PSS7 varchar(80) DEFAULT NULL,
   PSS9 varchar(80) DEFAULT NULL,
   CD4 varchar(80) DEFAULT NULL,
   CD4Baseline varchar(80) DEFAULT NULL,
   TB_LAM varchar(80) DEFAULT NULL,
   CRAG varchar(80) DEFAULT NULL,
   WHO_STAGE varchar(80) DEFAULT NULL,
   CURRENT_REGIMEN_START_DATE datetime DEFAULT NULL,
   PRIMARY KEY (ID)


) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO reporting_audit_tool_hiv1 (
    PATTIENT_NO ,
    HIV_ENROLLMENT_DATE ,
    TEST_TYPE ,
    CARE_ENTRY_POINT ,
    TEMP ,
    RESPIRATORY_RATE ,
    HEART_RATE ,
    CLIENT_CATEGORY ,
    MARITAL_STATUS ,
    REGISTRATION_DATE ,
    SIGNS_AND_SYMPTOMS ,
    SIDE_EFFECTS ,
    PSS4 ,
    PSS7 ,
    PSS9 ,
    CD4 ,
    CD4Baseline ,
    TB_LAM ,
    CRAG ,
    WHO_STAGE ,
    CURRENT_REGIMEN_START_DATE
)
SELECT patient,
       en_date,
       test_type.name,
       care_entry.name,
       TEMP.value_numeric,
       RR.value_numeric,
       HR.value_numeric,
       client_category.name,
       marital.name,
       art_summary_enc.visit_date,
       signs.name,
       side_effects.name,
       PPS4.name,
       PSS7.name,
       PSS9.name,
       CD4.value_numeric ,
       CD4Baseline.value_numeric,
       TB_LAM.name,
       CRAG.name,
       WHO_STAGE.name,
       start_date
FROM  (select DISTINCT e.patient_id as patient from encounter e INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid in('8d5b27bc-c2cc-11de-8d13-0010c6dffd0f','8d5b2be0-c2cc-11de-8d13-0010c6dffd0f') and encounter_datetime<= current_date() and encounter_datetime>= DATE_SUB(current_date(), INTERVAL 12 MONTH))cohort join
    person p on p.person_id = cohort.patient LEFT JOIN
    (SELECT person_id, max(DATE (value_datetime))as en_date FROM obs WHERE concept_id=165312 and voided=0 and  value_datetime<=current_date() AND obs_datetime <=current_date() group by person_id)enroll_date on patient=enroll_date.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99080 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=99080 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)test_type on patient= test_type.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99116 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=99116 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)care_entry on patient= care_entry.person_id
    LEFT JOIN (SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=5088 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=5088 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)TEMP on patient=TEMP.person_id
    LEFT JOIN (SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=5242 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=5242 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)RR on patient=RR.person_id
    LEFT JOIN (SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=5087 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=5087 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)HR on patient=HR.person_id
    LEFT JOIN (SELECT person_id, cn.name from person_attribute pa inner join person_attribute_type pat on pa.person_attribute_type_id = pat.person_attribute_type_id and  pat.uuid='dec484be-1c43-416a-9ad0-18bd9ef28929' LEFT JOIN concept_name cn ON value = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where pa.voided=0)client_category on patient =client_category.person_id
    LEFT JOIN (SELECT person_id, cn.name from person_attribute pa inner join person_attribute_type pat on pa.person_attribute_type_id = pat.person_attribute_type_id and  pat.uuid='8d871f2a-c2cc-11de-8d13-0010c6dffd0f' LEFT JOIN concept_name cn ON value = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where pa.voided=0)marital on patient =marital.person_id
    LEFT JOIN (select e.patient_id,max(DATE(encounter_datetime))visit_date from encounter e INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid in ('8d5b27bc-c2cc-11de-8d13-0010c6dffd0f') and encounter_datetime<= current_date() group by patient_id)as art_summary_enc on patient=art_summary_enc.patient_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90251 and voided=0 and value_coded not in (90002) group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=90251 and value_coded not in (90002) and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)signs on patient= signs.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90227 and voided=0 and value_coded not in (90002) group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=90227 and value_coded not in (90002) and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)side_effects on patient= side_effects.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165207 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165207 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)PPS4 on patient= PPS4.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99175 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=99175 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)PSS7 on patient= PSS7.person_id
    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165218 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165218 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)PSS9 on patient= PSS9.person_id
    LEFT JOIN (SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=5497 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=5497 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <=current_date() group by o.person_id)CD4 on patient=CD4.person_id
        LEFT JOIN (SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99071 and voided=0 group by person_id)A on o.person_id = A.person_id
    where o.concept_id=99071 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <=current_date() group by o.person_id)CD4Baseline on patient=CD4Baseline.person_id
        LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165291 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165291 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)TB_LAM on patient= TB_LAM.person_id
        LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165290 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=165290 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)CRAG on patient= CRAG.person_id
       LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90203 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'
    where o.concept_id=90203 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date() group by o.person_id)WHO_STAGE on patient= WHO_STAGE.person_id
    LEFT JOIN (SELECT B.person_id, min(DATE (B.obs_datetime)) as start_date from obs B inner join (SELECT o.person_id,o.value_coded from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90315
    and voided=0 group by person_id)A on o.person_id = A.person_id where o.concept_id=90315 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=current_date())C on B.person_id=C.person_id where B.value_coded=C.value_coded and obs_datetime<=current_date() and voided=0 group by B.person_id)REGIMEN_STARTDATE on patient= REGIMEN_STARTDATE.person_id;


DROP TABLE IF EXISTS reporting_audit_tool_eid;

CREATE TABLE reporting_audit_tool_eid (
   ID int(11) NOT NULL AUTO_INCREMENT,
   PATTIENT_NO INT(11) NOT NULL ,
   EDD VARCHAR(80) DEFAULT NULL ,
   EID_NO VARCHAR(80) DEFAULT NULL ,
   EID_DOB VARCHAR(80) DEFAULT NULL ,
   EID_AGE VARCHAR(80) DEFAULT NULL ,
   EID_WEIGHT VARCHAR(80) DEFAULT NULL ,
   EID_NEXT_APPT varchar(80) DEFAULT NULL,
   EID_FEEDING varchar(80) DEFAULT NULL,
   CTX_START varchar(80) DEFAULT NULL,
   CTX_AGE varchar(80) DEFAULT NULL,
   1ST_PCR_DATE varchar(80) DEFAULT NULL,
   1ST_PCR_AGE varchar(80) DEFAULT NULL,
   1ST_PCR_RESULT varchar(80) DEFAULT NULL,
   1ST_PCR_RECEIVED varchar(80) DEFAULT NULL,
   2ND_PCR_DATE varchar(80) DEFAULT NULL,
   2ND_PCR_AGE varchar(80) DEFAULT NULL,
   2ND_PCR_RESULT varchar(80) DEFAULT NULL,
   2ND_PCR_RECEIVED varchar(80) DEFAULT NULL,
   REPEAT_PCR_DATE varchar(80) DEFAULT NULL,
   REPEAT_PCR_AGE varchar(80) DEFAULT NULL,
   REPEAT_PCR_RESULT varchar(80) DEFAULT NULL,
   REPEAT_PCR_RECEIVED varchar(80) DEFAULT NULL,
   RAPID_PCR_DATE varchar(80) DEFAULT NULL,
   RAPID_PCR_AGE varchar(80) DEFAULT NULL,
   RAPID_PCR_RESULT varchar(80) DEFAULT NULL,
   FINAL_OUTCOME varchar(80) DEFAULT NULL,
   LINKAGE_NO varchar(80) DEFAULT NULL,
   NVP_AT_BIRTH varchar(80) DEFAULT NULL,
   PRIMARY KEY (ID)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# remove the not pregnant in the cohort

INSERT INTO reporting_audit_tool_eid (
    PATTIENT_NO,
    EDD ,
    EID_NO ,
    EID_DOB,
    EID_AGE ,
    EID_WEIGHT ,
    EID_NEXT_APPT ,
    EID_FEEDING ,
    CTX_START ,
    CTX_AGE ,
    1ST_PCR_DATE ,
    1ST_PCR_AGE ,
    1ST_PCR_RESULT ,
    1ST_PCR_RECEIVED ,
    2ND_PCR_DATE ,
    2ND_PCR_AGE ,
    2ND_PCR_RESULT ,
    2ND_PCR_RECEIVED ,
    REPEAT_PCR_DATE ,
    REPEAT_PCR_AGE ,
    REPEAT_PCR_RESULT ,
    REPEAT_PCR_RECEIVED ,
    RAPID_PCR_DATE ,
    RAPID_PCR_AGE ,
    RAPID_PCR_RESULT ,
    FINAL_OUTCOME ,
    LINKAGE_NO ,
    NVP_AT_BIRTH
)
SELECT patient,
    IFNULL(EDD.edd_date,''),
    IFNULL(EIDNO.id,'') as EIDNO,
    IFNULL(EIDDOB.dob,'') as EID_DOB,
    IFNULL(TIMESTAMPDIFF(MONTH , EIDDOB.dob, CURRENT_DATE()),'') as EID_age,
    IFNULL(EID_W.value_numeric,'') as EID_Weight,
    IFNULL(EID_NEXT_APPT.value_datetime,'')AS NEXT_APPOINTMENT_DATE,
    IFNULL(EID_FEEDING.name,'') as Feeding,
    IFNULL(CTX.mydate,'') as CTX_START,
    IFNULL(TIMESTAMPDIFF(MONTH, CTX.mydate, CURRENT_DATE()),'') as agectx,
    IFNULL(1stPCR.mydate,'') as 1stPCRDATE,
    IFNULL(TIMESTAMPDIFF(MONTH, 1stPCR.mydate, CURRENT_DATE()),'') as age1stPCR,
    IFNULL(1stPCRResult.name,''),
    IFNULL(1stPCRReceived.mydate,'') as 1stPCRRecieved,
    IFNULL(2ndPCR.mydate,'') as 2ndPCRDATE,
    IFNULL(TIMESTAMPDIFF(MONTH, 2ndPCR.mydate, CURRENT_DATE()),'') as age2ndPCR,
    IFNULL(2ndPCRResult.name,''),
    IFNULL(2ndPCRReceived.mydate,'') as 2ndPCRRecieved,
    IFNULL(repeatPCR.mydate,'') as repeatPCRDATE,
    IFNULL(TIMESTAMPDIFF(MONTH, repeatPCR.mydate, CURRENT_DATE()),'') as age3rdPCR,
    IFNULL(repeatPCRResult.name,''),
    IFNULL(repeatPCRReceived.mydate,'') as repeatPCRRecieved,
    IFNULL(rapidTest.mydate,'') as rapidTestDate,
    IFNULL(TIMESTAMPDIFF(MONTH, rapidTest.mydate, CURRENT_DATE()),'') as ageatRapidTest,
    IFNULL(rapidTestResult.name,''),
    IFNULL(finalOutcome.name,''),
    IFNULL(linkageNo.value_text,''),
    IF(NVP.mydate IS NULL,'', IF(TIMESTAMPDIFF(DAY , NVP.mydate, CURRENT_DATE())<=2,'Y','N')) as NVP

    FROM  ( select DISTINCT o.person_id as patient from obs o WHERE o.voided = 0 and concept_id=90041 and value_coded in (1065,99601) and obs_datetime<= CURRENT_DATE() and obs_datetime>= DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) union
                SELECT person_a as patient from relationship r inner join person p on r.person_a = p.person_id inner join relationship_type rt on r.relationship = rt.relationship_type_id and rt.uuid='8d91a210-c2cc-11de-8d13-0010c6dffd0f' where p.gender='F' and r.person_b in (SELECT DISTINCT e.patient_id from encounter e INNER JOIN encounter_type et
                ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid in('9fcfcc91-ad60-4d84-9710-11cc25258719','4345dacb-909d-429c-99aa-045f2db77e2b') and encounter_datetime<= CURRENT_DATE() and encounter_datetime>= DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR))) cohort join
        person p on p.person_id = cohort.patient
        LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as edd_date FROM obs WHERE concept_id=5596 and voided=0 and  obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_id)EDD on patient=EDD.person_id
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')and concept_id=99771 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
        on o.person_id = A.person_b where o.concept_id=99771 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) NVP on patient = NVP.parent
        LEFT JOIN (SELECT person_a as parent,pi.identifier as id  from relationship left join patient_identifier pi on person_b = patient_id inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR) INNER JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id and pit.uuid='2c5b695d-4bf3-452f-8a7c-fe3ee3432ffe'  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f') and pi.voided=0) EIDNO on patient = EIDNO.parent
        LEFT JOIN (SELECT person_a as parent,p.birthdate as dob  from relationship inner join person p on person_b = p.person_id and p.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR) where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f') ) EIDDOB on patient = EIDDOB.parent
        LEFT JOIN (SELECT parent,value_numeric  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=5089 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
        on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=5089 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) EID_W on patient = EID_W.parent
        LEFT JOIN (SELECT parent,value_datetime from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=5096 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=5096 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) EID_NEXT_APPT on patient = EID_NEXT_APPT.parent
        LEFT JOIN (SELECT parent,cn.name from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99451 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99451 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) EID_FEEDING on patient = EID_FEEDING.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99773 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99773 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) CTX on patient = CTX.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99606 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b where o.concept_id=99606 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 1stPCR on patient = 1stPCR.parent
        LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)   where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99435 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99435 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 1stPCRResult on patient = 1stPCRResult.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99438 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=99438 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 1stPCRReceived on patient = 1stPCRReceived.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99436 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=99436 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 2ndPCR on patient = 2ndPCR.parent
        LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99440 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99440 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 2ndPCRResult on patient = 2ndPCRResult.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99442 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=99442 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) 2ndPCRReceived on patient = 2ndPCRReceived.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=165405 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=165405 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) repeatPCR on patient = repeatPCR.parent
        LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=165406 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=165406 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) repeatPCRResult on patient = repeatPCRResult.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=165408 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=165408 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) repeatPCRReceived on patient = repeatPCRReceived.parent
        LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=162879 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=162879 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) rapidTest on patient = rapidTest.parent
        LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=162880 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=162880 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) rapidTestResult on patient = rapidTestResult.parent
        LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
        and concept_id=99797 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99797 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) finalOutcome on patient = finalOutcome.parent
        LEFT JOIN (SELECT parent,value_text  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b inner join person p2 on relationship.person_b = p2.person_id and p2.birthdate >= DATE_SUB(CURRENT_DATE(), INTERVAL 2 YEAR)  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')
           and concept_id=99751 and obs.voided=0 and obs_datetime<=CURRENT_DATE() AND obs_datetime >=DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) group by person_b)A
on o.person_id = A.person_b where o.concept_id=99751 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <=CURRENT_DATE() group by parent) linkageNo on patient = linkageNo.parent;
