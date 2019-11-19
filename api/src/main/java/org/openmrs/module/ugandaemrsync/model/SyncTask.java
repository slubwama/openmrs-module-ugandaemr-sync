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
	private String syncTask;

	@Column(name = "status", length = 255)
	private String status;

	@Column(name = "status_code", length = 11)
	private int statusCode;

	@Column(name = "sent_to_url", length = 255)
	private String sentToUrl;

	@Column(name = "date_sent")
	private Date dateSent;

	@Column(name = "require_action")
	Boolean requireAction;

	@Column(name = "action_completed")
	Boolean actionCompleted;

	public Boolean getRequireAction() {
		return requireAction;
	}

	public void setRequireAction(Boolean requireAction) {
		this.requireAction = requireAction;
	}

	public Boolean getActionCompleted() {
		return actionCompleted;
	}

	public void setActionCompleted(Boolean actionCompleted) {
		this.actionCompleted = actionCompleted;
	}

	public SyncTaskType getSyncTaskType() {
		return syncTaskType;
	}

	public void setSyncTaskType(SyncTaskType syncTaskType) {
		this.syncTaskType = syncTaskType;
	}

	public String getSyncTask() {
		return syncTask;
	}

	public void setSyncTask(String syncTask) {
		this.syncTask = syncTask;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getSentToUrl() {
		return sentToUrl;
	}

	public void setSentToUrl(String sentToUrl) {
		this.sentToUrl = sentToUrl;
	}

	public Date getDateSent() {
		return dateSent;
	}

	public void setDateSent(Date dateSent) {
		this.dateSent = dateSent;
	}

	@Override
	public Integer getId() {
		return syncTaskId;
	}

	@Override
	public void setId(Integer id) {

	}
}
