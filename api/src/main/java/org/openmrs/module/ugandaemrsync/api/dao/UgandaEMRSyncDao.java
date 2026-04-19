/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.api.dao;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.model.SyncFhirResource;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfileLog;
import org.openmrs.module.ugandaemrsync.model.SyncFhirCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UgandaEMRSyncDao {

    DbSessionFactory sessionFactory;

    /**
     * @return
     */
    private DbSession getSession() {
        return getSessionFactory().getCurrentSession();
    }


    public DbSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(DbSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * /**
     *
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllSyncTaskType()
     */
    public List<SyncTaskType> getAllSyncTaskType() {
        return (List<SyncTaskType>) getSession().createCriteria(SyncTaskType.class).list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveSyncTaskType(org.openmrs.module.ugandaemrsync.model.SyncTaskType)
     */
    public SyncTaskType getSyncTaskTypeByUUID(String uuid) {
        return (SyncTaskType) getSession().createCriteria(SyncTaskType.class).add(Restrictions.eq("uuid", uuid))
                .uniqueResult();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveSyncTaskType(org.openmrs.module.ugandaemrsync.model.SyncTaskType)
     */
    public SyncTaskType saveSyncTaskType(SyncTaskType syncTaskType) {
        getSession().saveOrUpdate(syncTaskType);
        return syncTaskType;
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncTaskBySyncTaskId(java.lang.String)
     */
    public SyncTask getSyncTask(String syncTask) {
        return (SyncTask) getSession().createCriteria(SyncTask.class).add(Restrictions.eq("syncTask", syncTask))
                .uniqueResult();
    }



    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncTasksBySyncTaskId(java.lang.String)
     */
    public List<SyncTask> getSyncTasks(String syncTask) {
        return (List<SyncTask>) getSession().createCriteria(SyncTask.class).add(Restrictions.eq("syncTask", syncTask)).list();
    }

    /**
     * /**
     *
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllSyncTask()
     */
    public List<SyncTask> getAllSyncTask() {
        return (List<SyncTask>) getSession().createCriteria(SyncTask.class).list();
    }


    /**
     * /**
     *
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveSyncTask(org.openmrs.module.ugandaemrsync.model.SyncTask)
     */
    public SyncTask saveSyncTask(SyncTask syncTask) {
        getSession().saveOrUpdate(syncTask);
        return syncTask;
    }

    /**
     * @param query
     * @return
     */
    public List getDatabaseRecord(String query) {
        SQLQuery sqlQuery = getSession().createSQLQuery(query);
        return sqlQuery.list();
    }

    /**
     * @param columns
     * @param finalQuery
     * @return
     */
    public List getFinalResults(List<String> columns, String finalQuery) {
        SQLQuery sqlQuery = getSession().createSQLQuery(finalQuery);
        for (String column : columns) {
            sqlQuery.addScalar(column, StringType.INSTANCE);
        }
        return sqlQuery.list();
    }

    /**
     * /**
     *
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getIncompleteActionSyncTask(java.lang.String)
     */
    public List<SyncTask> getIncompleteActionSyncTask(String syncTaskTypeUuid) {
        SQLQuery sqlQuery = getSession()
                .createSQLQuery(
                        "select sync_task.* from sync_task inner join sync_task_type on (sync_task_type.sync_task_type_id=sync_task.sync_task_type) where sync_task_type.uuid='"
                                + syncTaskTypeUuid
                                + "' and  require_action=true and action_completed=false;");
        sqlQuery.addEntity(SyncTask.class);
        return sqlQuery.list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileById(java.lang.Integer)
     */
    public SyncFhirProfile getSyncFhirProfileById(Integer id) {
        return (SyncFhirProfile) getSession().createCriteria(SyncFhirProfile.class).add(Restrictions.eq("syncFhirProfileId", id))
                .uniqueResult();
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileByUUID(java.lang.String)
     */
    public SyncFhirProfile getSyncFhirProfileByUUID(String uuid) {
        return (SyncFhirProfile) getSession().createCriteria(SyncFhirProfile.class).add(Restrictions.eq("uuid", uuid))
                .uniqueResult();
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveSyncFhirProfile(SyncFhirProfile)
     */
    public SyncFhirProfile saveSyncFhirProfile(SyncFhirProfile syncFhirProfile) {
        getSession().saveOrUpdate(syncFhirProfile);
        return syncFhirProfile;
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveFHIRResource(SyncFhirResource)
     */
    public SyncFhirResource saveSyncFHIRResource(SyncFhirResource syncFHIRResource) {
        getSession().saveOrUpdate(syncFHIRResource);

        return syncFHIRResource;

    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveFHIRResource(SyncFhirResource)
     */
    public List<SyncFhirResource> getSyncResourceBySyncFhirProfile(SyncFhirProfile syncFhirProfile, boolean includeSynced) {

        Criteria criteria = getSession().createCriteria(SyncFhirResource.class);

        if (syncFhirProfile != null) {
            criteria.add(Restrictions.eq("generatorProfile", syncFhirProfile));
        }

        if (!includeSynced) {
            criteria.add(Restrictions.eq("synced", false));
        }

        criteria.addOrder(Order.desc("dateCreated"));

        return criteria.list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFHIRResourceById(java.lang.Integer)
     */
    public SyncFhirResource getSyncFHIRResourceById(Integer id) {

        return (SyncFhirResource) getSession().createCriteria(SyncFhirResource.class).add(Restrictions.eq("resourceId", id))
                .uniqueResult();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#purgeExpiredFHIRResource(java.util.Date)
     */
    public void purgeExpiredFHIRResource(SyncFhirResource syncFHIRResource) {
        getSession().delete(syncFHIRResource);
    }

    public List<SyncFhirResource> getExpiredSyncFHIRResources(Date date) {

        Criteria criteria = getSession().createCriteria(SyncFhirResource.class).add(Restrictions.le("expiryDate", date));

        criteria.addOrder(Order.desc("expiryDate"));

        return criteria.list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveSyncFhirProfileLog(SyncFhirProfileLog)
     */
    public SyncFhirProfileLog saveSyncFhirProfileLog(SyncFhirProfileLog syncFhirProfileLog) {
        getSession().saveOrUpdate(syncFhirProfileLog);
        return syncFhirProfileLog;
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileLogByProfileAndResourceName(SyncFhirProfile, java.lang.String)
     */
    public List<SyncFhirProfileLog> getSyncFhirProfileLogByProfileAndResourceName(SyncFhirProfile syncFhirProfile, String resourceType) {
        Criteria criteria = getSession().createCriteria(SyncFhirProfileLog.class);

        if (syncFhirProfile != null && resourceType != null) {
            criteria.add(Restrictions.eq("resourceType", resourceType));
            criteria.add(Restrictions.eq("profile", syncFhirProfile));
            criteria.addOrder(Order.desc("dateCreated"));
        }

        return criteria.list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFHIRCaseBySyncFhirProfileAndPatient(SyncFhirProfile, org.openmrs.Patient, java.lang.String)
     */
    public SyncFhirCase getSyncFHIRCaseBySyncFhirProfileAndPatient(SyncFhirProfile syncFhirProfile, Patient patient, String caseIdentifier) {
        Criteria criteria = getSession().createCriteria(SyncFhirCase.class);
        criteria.add(Restrictions.eq("profile", syncFhirProfile));
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.eq("caseIdentifier", caseIdentifier));

        return (SyncFhirCase) criteria.uniqueResult();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveSyncFHIRCase(SyncFhirCase)
     */
    public SyncFhirCase saveSyncFHIRCase(SyncFhirCase syncFHIRCase) {

        getSession().saveOrUpdate(syncFHIRCase);

        return syncFHIRCase;
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllSyncFhirProfile()
     */
    public List<SyncFhirProfile> getAllSyncFhirProfile() {
        return (List<SyncFhirProfile>) getSession().createCriteria(SyncFhirProfile.class).list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getUnSyncedFHirResources(org.openmrs.module.ugandaemrsync.model.SyncFhirProfile)
     */

    public List<SyncFhirResource> getUnSyncedFHirResources(SyncFhirProfile syncFhirProfile) {

        Criteria criteria = getSession().createCriteria(SyncFhirResource.class);
        criteria.add(Restrictions.eq("generatorProfile", syncFhirProfile));
        criteria.add(Restrictions.eq("synced", false));
        if (syncFhirProfile.getSyncLimit() != null) {
            criteria.setMaxResults(syncFhirProfile.getSyncLimit());
        }
        return criteria.list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirCasesByProfile(org.openmrs.module.ugandaemrsync.model.SyncFhirProfile)
     */
    public List<SyncFhirCase> getSyncFhirCasesByProfile(SyncFhirProfile syncFhirProfile) {

        Criteria criteria = getSession().createCriteria(SyncFhirCase.class);
        criteria.add(Restrictions.eq("profile", syncFhirProfile));
        return criteria.list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileByName(java.lang.String)
     */
    public List<SyncFhirProfile> getSyncFhirProfileByName(String name) {
        Criteria criteria = getSession().createCriteria(SyncFhirProfile.class);
        criteria.add(Restrictions.eq("name", name));
        return criteria.list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllSyncFhirProfileLog()
     */
    public List<SyncFhirProfileLog> getAllSyncFhirProfileLog() {
        return (List<SyncFhirProfileLog>) getSession().createCriteria(SyncFhirProfileLog.class).list();
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllSyncFhirCase()
     */
    public List<SyncFhirCase> getAllSyncFhirCase() {
        return (List<SyncFhirCase>) getSession().createCriteria(SyncFhirCase.class).list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirCaseByUUDI(java.lang.String)
     */
    public SyncFhirCase getSyncFhirCaseByUUDI(String uuid) {
        Criteria criteria = getSession().createCriteria(SyncFhirProfile.class);
        criteria.add(Restrictions.eq("uuid", uuid));
        return (SyncFhirCase) criteria.uniqueResult();
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllFHirResources()
     */
    public List<SyncFhirResource> getAllFHirResources() {
        return (List<SyncFhirResource>) getSession().createCriteria(SyncFhirResource.class).list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirResourceByUUID(java.lang.String)
     */
    public SyncFhirResource getSyncFhirResourceByUUID(String uuid) {
        Criteria criteria = getSession().createCriteria(SyncFhirResource.class);
        criteria.add(Restrictions.eq("uuid", uuid));
        return (SyncFhirResource) criteria.uniqueResult();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileLogByProfile(org.openmrs.module.ugandaemrsync.model.SyncFhirProfile)
     */
    public List<SyncFhirProfileLog> getSyncFhirProfileLogByProfile(SyncFhirProfile syncFhirProfile) {
        Criteria criteria = getSession().createCriteria(SyncFhirProfileLog.class);
        criteria.add(Restrictions.eq("profile", syncFhirProfile));
        return criteria.list();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileLogByUUID(java.lang.String)
     */
    public SyncFhirProfileLog getSyncFhirProfileLogByUUID(String uuid) {
        Criteria criteria = getSession().createCriteria(SyncFhirProfileLog.class);
        criteria.add(Restrictions.eq("uuid", uuid));
        return (SyncFhirProfileLog) criteria.uniqueResult();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileLogById(java.lang.Integer)
     */
    public SyncFhirProfileLog getSyncFhirProfileLogById(Integer id) {

        Criteria criteria = getSession().createCriteria(SyncFhirProfileLog.class);
        criteria.add(Restrictions.eq("profileLogId", id));
        return (SyncFhirProfileLog) criteria.uniqueResult();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirCaseById(java.lang.Integer)
     */
    public SyncFhirCase getSyncFhirCaseById(Integer id) {
        Criteria criteria = getSession().createCriteria(SyncFhirCase.class);
        criteria.add(Restrictions.eq("profileLogId", id));
        return (SyncFhirCase) criteria.uniqueResult();
    }

    public List<SyncFhirResource> getSyncedFHirResources(SyncFhirProfile syncFhirProfile) {

        Criteria criteria = getSession().createCriteria(SyncFhirResource.class);
        criteria.add(Restrictions.eq("generatorProfile", syncFhirProfile));
        criteria.add(Restrictions.eq("synced", true));

        return criteria.list();
    }

    public List<SyncFhirResource> getSyncedFHirResources(SyncFhirProfile syncFhirProfile, Date startTime, Date endTime) {

        Criteria criteria = getSession().createCriteria(SyncFhirResource.class);
        criteria.add(Restrictions.eq("generatorProfile", syncFhirProfile));
        criteria.add(Restrictions.eq("synced", true));
        criteria.add(Restrictions.between("dateSynced", startTime, endTime));

        return criteria.list();
    }

    public List<SyncTaskType> getSyncTaskTypeByName(String name) {
        Criteria criteria = getSession().createCriteria(SyncTaskType.class);
        criteria.add(Restrictions.eq("name", name));
        return criteria.list();
    }

    public SyncTaskType getSyncTaskTypeById(Integer id) {
        Criteria criteria = getSession().createCriteria(SyncTaskType.class);
        criteria.add(Restrictions.eq("id", id));
        return (SyncTaskType) criteria.uniqueResult();
    }

    public List<SyncTask> getSyncTasksByType(SyncTaskType syncTaskType, Date synceDateFrom, Date synceDateTo) {
        Criteria criteria = getSession().createCriteria(SyncTask.class);
        criteria.add(Restrictions.eq("syncTaskType", syncTaskType));
        criteria.add(Restrictions.between("dateCreated", synceDateFrom, synceDateTo));
        return criteria.list();
    }

    public List<SyncTask> getSyncTasksByType(SyncTaskType syncTaskType) {
        Criteria criteria = getSession().createCriteria(SyncTask.class);
        criteria.add(Restrictions.eq("syncTaskType", syncTaskType));
        return criteria.list();
    }

    public SyncTask getSyncTaskByUUID(String uniqueId) {
        Criteria criteria = getSession().createCriteria(SyncTask.class);
        criteria.add(Restrictions.eq("uuid", uniqueId));
        return (SyncTask) criteria.uniqueResult();
    }

    public SyncTask getSyncTaskById(Integer uniqueId) {
        Criteria criteria = getSession().createCriteria(SyncTask.class);
        criteria.add(Restrictions.eq("id", uniqueId));
        return (SyncTask) criteria.uniqueResult();
    }

    public List<SyncFhirResource> getSyncResourceBySyncFhirProfile(SyncFhirProfile syncFhirProfile, String from, String to) {
        to =to +" 23:59:59";
        String query ="select resource_id, synced, date_synced, expiry_date, generator_profile, NULL as resource, sfr.creator, sfr.date_created, sfr.changed_by, sfr.date_changed, sfr.voided, sfr.date_voided, sfr.voided_by, sfr.void_reason, sfr.uuid, sfr.statusCode, status_code_detail, patient_id from sync_fhir_resource sfr inner join sync_fhir_profile sfp on sfr.generator_profile = sfp.sync_fhir_profile_id where sfp.uuid='" + syncFhirProfile.getUuid()
                + "' and sfr.date_created >='"+from +"'"+"and sfr.date_created <='"+to +"';" ;
        SQLQuery sqlQuery = getSession()
                .createSQLQuery(query);
        System.out.println(query);
        sqlQuery.addEntity(SyncFhirResource.class);
        return sqlQuery.list();

    }

    public List<SyncTask> searchSyncTask(SyncTaskType syncTaskType, Integer statusCode, Date fromDate, Date toDate) {

        Criteria criteria = getSession().createCriteria(SyncTask.class);


        if (syncTaskType!= null) {
            criteria.add(Restrictions.eq("syncTaskType", syncTaskType));
        }

        if (statusCode!= null) {
            criteria.add(Restrictions.eq("statusCode", statusCode));
        }

        if (fromDate != null && toDate != null) {
            criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
        }
        return criteria.list();
    }

    public void deleteUnSuccessfulSyncTasks(String syncTask, SyncTaskType syncTaskType) {
        Context.getAdministrationService().executeSQL(String.format("delete from sync_task where status_code != %s and sync_task = '%s' and sync_task_type= %s",200,syncTask,syncTaskType.getSyncTaskTypeId()),false);
    }

    /**
     * Secure method to get patients by order type and date
     * @param orderTypeId the order type ID
     * @param dateFrom the start date
     * @return list of patient IDs
     */
    public List<Integer> getPatientsByOrderTypeAndDate(Integer orderTypeId, Date dateFrom) {
        String sql = "SELECT DISTINCT patient_id FROM orders WHERE order_type_id = ? AND date_activated >= ? AND date_stopped IS NULL";
        SQLQuery sqlQuery = getSession().createSQLQuery(sql);
        sqlQuery.setParameter(0, orderTypeId);
        sqlQuery.setParameter(1, dateFrom);
        return sqlQuery.list();
    }

    /**
     * Secure method to get patients by identifier type excluding existing cases
     * @param identifierTypeId the patient identifier type ID
     * @param profileId the sync profile ID
     * @return list of patient IDs
     */
    public List<Integer> getPatientsByIdentifierTypeExcludingProfile(Integer identifierTypeId, Integer profileId) {
        String sql = "SELECT patient_id FROM patient_identifier WHERE identifier_type = ? AND patient_id NOT IN (SELECT patient FROM sync_fhir_case WHERE profile = ?)";
        SQLQuery sqlQuery = getSession().createSQLQuery(sql);
        sqlQuery.setParameter(0, identifierTypeId);
        sqlQuery.setParameter(1, profileId);
        return sqlQuery.list();
    }

    /**
     * Secure method to get patients by cohort type
     * @param cohortTypeUuid the cohort type UUID
     * @return list of patient IDs
     */
    public List<Integer> getPatientsByCohortType(String cohortTypeUuid) {
        String sql = "SELECT cm.patient_id FROM cohort_member cm " +
                     "INNER JOIN cohort c ON cm.cohort_id = c.cohort_id " +
                     "INNER JOIN cohort_type ct ON c.cohort_type_id = ct.cohort_type_id " +
                     "WHERE ct.uuid = ? AND c.voided = 0 AND cm.voided = 0";
        SQLQuery sqlQuery = getSession().createSQLQuery(sql);
        sqlQuery.setParameter(0, cohortTypeUuid);
        return sqlQuery.list();
    }

    /**
     * Secure method to get incomplete action sync tasks
     * @param syncTaskTypeUuid the sync task type UUID
     * @return list of sync tasks
     */
    public List<SyncTask> getIncompleteActionSyncTasksSecure(String syncTaskTypeUuid) {
        String sql = "SELECT sync_task.* FROM sync_task " +
                     "INNER JOIN sync_task_type ON (sync_task_type.sync_task_type_id = sync_task.sync_task_type) " +
                     "WHERE sync_task_type.uuid = ? AND require_action = true AND action_completed = false";
        SQLQuery sqlQuery = getSession().createSQLQuery(sql);
        sqlQuery.setParameter(0, syncTaskTypeUuid);
        sqlQuery.addEntity(SyncTask.class);
        return sqlQuery.list();
    }

    /**
     * Secure method to get synced FHIR resources by profile and date range
     * @param profileUuid the sync profile UUID
     * @param from start date
     * @param to end date
     * @return list of sync FHIR resources
     */
    /**
     * Secure method to get viral load orders with accession numbers
     * Optimized to eliminate N+1 query pattern and includes time boundary for performance
     * @param days Number of days to look back for orders (prevents querying all historical data)
     * @return list of Order IDs for viral load orders with accession numbers within the time boundary
     */
    public List<Integer> getViralLoadOrderIdsWithAccessionNumbers(int days) {
        String sql = "SELECT orders.order_id FROM orders " +
                     "INNER JOIN test_order ON (test_order.order_id = orders.order_id) " +
                     "WHERE accession_number IS NOT NULL " +
                     "AND specimen_source IS NOT NULL " +
                     "AND orders.instructions = 'REFER TO cphl' " +
                     "AND orders.concept_id = 165412 " +
                     "AND date_stopped IS NULL " +
                     "AND orders.date_created >= DATE_SUB(CURDATE(), INTERVAL ? DAY)";

        SQLQuery sqlQuery = getSession().createSQLQuery(sql);
        sqlQuery.setParameter(0, days);
        List<Object> results = sqlQuery.list();

        List<Integer> orderIds = new ArrayList<>();
        for (Object result : results) {
            if (result != null) {
                orderIds.add(Integer.parseInt(result.toString()));
            }
        }
        return orderIds;
    }

    /**
     * Batch get orders by IDs to avoid N+1 queries
     * @param orderIds list of order IDs to fetch
     * @return list of Orders
     */
    public List<org.openmrs.Order> getOrdersByIds(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return new ArrayList<>();
        }

        Criteria criteria = getSession().createCriteria(org.openmrs.Order.class);
        criteria.add(Restrictions.in("orderId", orderIds));
        criteria.add(Restrictions.eq("voided", false));
        return criteria.list();
    }
}
