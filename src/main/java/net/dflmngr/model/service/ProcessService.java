package net.dflmngr.model.service;

import java.time.ZonedDateTime;
import java.util.List;

import net.dflmngr.model.entity.Process;
import net.dflmngr.model.entity.keys.ProcessPK;

public interface ProcessService extends GenericService<Process, ProcessPK> {
	public List<Process> getProcessById(String processId);
	public List<Process> getProcess(String processId, ZonedDateTime time);
}
