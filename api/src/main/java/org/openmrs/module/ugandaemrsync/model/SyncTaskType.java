package org.openmrs.module.ugandaemrsync.model;

import org.openmrs.BaseOpenmrsData;

import javax.persistence.*;

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
	
	@Override
	public Integer getId() {
		return syncTaskTypeId;
	}
	
	@Override
	public void setId(Integer id) {
		this.syncTaskTypeId = id;
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
}
