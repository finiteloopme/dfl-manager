package net.dflmngr.model.service.impl;

import java.time.ZonedDateTime;
import java.util.List;

import net.dflmngr.model.dao.ProcessDao;
import net.dflmngr.model.dao.impl.ProcessDaoImpl;
import net.dflmngr.model.entity.Process;
import net.dflmngr.model.entity.keys.ProcessPK;
import net.dflmngr.model.service.ProcessService;

public class ProcessServiceImpl extends GenericServiceImpl<Process, ProcessPK> implements ProcessService {
	private ProcessDao dao;
	
	public ProcessServiceImpl() {
		dao = new ProcessDaoImpl();
		setDao(dao);
	}

	public List<Process> getProcessById(String processId) {
		List<Process> process = dao.findProcessById(processId);
		return process;
	}
	
	public List<Process> getProcess(String processId, ZonedDateTime time) {
		List<Process> process = dao.findProcess(processId, time);
		return process;
	}
}
