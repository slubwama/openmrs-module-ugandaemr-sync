package org.openmrs.module.ugandaemrsync.model;

import org.openmrs.BaseOpenmrsData;

import javax.persistence.Basic;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Entity;

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
	
	@Override
	public Integer getId() {
		return syncTaskTypeId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void setId(Integer id) {
		this.syncTaskTypeId = id;
	}
}
