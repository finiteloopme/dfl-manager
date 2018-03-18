package net.dflmngr.model.entity;

import java.time.ZonedDateTime;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
//import javax.persistence.IdClass;
import javax.persistence.Table;

//import net.dflmngr.model.entity.keys.ProcessPK;

@Entity
@Cacheable(false)
@Table(name="process")
//@IdClass(ProcessPK.class)
public class Process {

	@Id
	@Column(name="process_id")
	private String processId;
	
	//@Id
	@Column(name="start_time")
	private ZonedDateTime startTime;
	
	@Column(name="end_time")
	private ZonedDateTime endTime;

	private String params;

	
	private String status;


	public String getProcessId() {
		return processId;
	}


	public void setProcessId(String processId) {
		this.processId = processId;
	}


	public ZonedDateTime getStartTime() {
		return startTime;
	}


	public void setStartTime(ZonedDateTime startTime) {
		this.startTime = startTime;
	}


	public ZonedDateTime getEndTime() {
		return endTime;
	}


	public void setEndTime(ZonedDateTime endTime) {
		this.endTime = endTime;
	}


	public String getParams() {
		return params;
	}


	public void setParams(String params) {
		this.params = params;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	@Override
	public String toString() {
		return "Process [processId=" + processId + ", startTime=" + startTime + ", endTime=" + endTime + ", params="
				+ params + ", status=" + status + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((processId == null) ? 0 : processId.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Process other = (Process) obj;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (processId == null) {
			if (other.processId != null)
				return false;
		} else if (!processId.equals(other.processId))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}
}
