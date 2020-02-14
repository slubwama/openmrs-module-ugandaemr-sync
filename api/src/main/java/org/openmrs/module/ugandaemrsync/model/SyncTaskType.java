package org.openmrs.module.ugandaemrsync.model;

import org.openmrs.BaseOpenmrsData;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.Basic;

@Entity(name = "ugandaemrsync.SyncTaskType")
@Table(name = "sync_task_type")
public class SyncTaskType extends BaseOpenmrsData {

	@Id
	@GeneratedValue
	@Column(name = "sync_task_type_id")
	private int syncTaskTypeId;

	@Basic
	@Column(name = "name", length = 255)
	private String name;

	@Column(name = "data_type", length = 50)
	private String dataType;

	@Column(name = "data_type_id", length = 255)
	private String dataTypeId;

	@Column(name = "url_end_point", length = 255)
	private String url;

	@Column(name = "url_token", length = 255)
	private String urlToken;

	@Column(name = "url_username", length = 255)
	private String urlUserName;

	@Column(name = "url_password", length = 255)
	private String urlPassword;

	public int getSyncTaskTypeId() {
		return syncTaskTypeId;
	}

	public void setSyncTaskTypeId(int syncTaskTypeId) {
		this.syncTaskTypeId = syncTaskTypeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDataTypeId() {
		return dataTypeId;
	}

	public void setDataTypeId(String dataTypeId) {
		this.dataTypeId = dataTypeId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlToken() {
		return urlToken;
	}

	public void setUrlToken(String urlToken) {
		this.urlToken = urlToken;
	}

	public String getUrlUserName() {
		return urlUserName;
	}

	public void setUrlUserName(String urlUserName) {
		this.urlUserName = urlUserName;
	}

	public String getUrlPassword() {
		return urlPassword;
	}

	public void setUrlPassword(String urlPassword) {
		this.urlPassword = urlPassword;
	}

	@Override
	public Integer getId() {
		return syncTaskTypeId;
	}

	@Override
	public void setId(Integer id) {
		this.syncTaskTypeId = syncTaskTypeId;
	}
}
