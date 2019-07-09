package org.openmrs.module.ugandaemrsync.model;

import org.openmrs.BaseOpenmrsData;


import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity(name = "ugandaemrsync.SyncTask")
@Table(name = "sync_task")
public class SyncTask extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue
	@Column(name = "sync_task_id")
	private int syncTaskId;
	
	@ManyToOne
	@JoinColumn(name = "sync_task_type")
	private SyncTaskType syncTaskType;
	
	@Column(name = "sync_task", length = 255)
	private int SyncTask;
	
	@Column(name = "status", length = 255)
	private String Status;
	
	@Column(name = "status_code", length = 11)
	private int StatusCode;
	
	@Column(name = "sent_to_url", length = 255)
	private String SentToUrl;
	
	@Column(name = "date_sent")
	private Date DateSent;
	
	public SyncTaskType getSyncTaskType() {
		return syncTaskType;
	}
	
	public void setSyncTaskType(SyncTaskType syncTaskType) {
		this.syncTaskType = syncTaskType;
	}
	
	public int getSyncTask() {
		return SyncTask;
	}
	
	public void setSyncTask(int syncTask) {
		SyncTask = syncTask;
	}
	
	public String getStatus() {
		return Status;
	}
	
	public void setStatus(String status) {
		Status = status;
	}
	
	public int getStatusCode() {
		return StatusCode;
	}
	
	public void setStatusCode(int statusCode) {
		StatusCode = statusCode;
	}
	
	public String getSentToUrl() {
		return SentToUrl;
	}
	
	public void setSentToUrl(String sentToUrl) {
		SentToUrl = sentToUrl;
	}
	
	public Date getDateSent() {
		return DateSent;
	}
	
	public void setDateSent(Date dateSent) {
		DateSent = dateSent;
	}
	
	@Override
	public Integer getId() {
		return syncTaskId;
	}
	
	@Override
	public void setId(Integer id) {
		
	}
}
