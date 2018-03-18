package net.dflmngr.model.dao;

import java.time.ZonedDateTime;
import java.util.List;

import net.dflmngr.model.entity.Process;
//import net.dflmngr.model.entity.keys.ProcessPK;

//public interface ProcessDao extends GenericDao<Process, ProcessPK> {
public interface ProcessDao extends GenericDao<Process, String> {
	public List<Process> findProcessById(String processId);
	public List<Process> findProcess(String processId, ZonedDateTime time);
}
