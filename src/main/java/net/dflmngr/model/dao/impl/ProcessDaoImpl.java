package net.dflmngr.model.dao.impl;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.criteria.Predicate;

import net.dflmngr.model.dao.ProcessDao;
import net.dflmngr.model.entity.Process;
import net.dflmngr.model.entity.Process_;
import net.dflmngr.model.entity.keys.ProcessPK;

public class ProcessDaoImpl extends GenericDaoImpl<Process, ProcessPK> implements ProcessDao {
	public ProcessDaoImpl() {
		super(Process.class);
	}

	public List<Process> findProcessById(String processId) {
		criteriaBuilder = entityManager.getCriteriaBuilder();
		criteriaQuery = criteriaBuilder.createQuery(entityClass);
		entity = criteriaQuery.from(entityClass);
		
		Predicate processIdEquals = criteriaBuilder.equal(entity.get(Process_.processId), processId);
		
		criteriaQuery.where(processIdEquals);
		List<Process> entitys = entityManager.createQuery(criteriaQuery).getResultList();
		
		return entitys;
	}
	
	public List<Process> findProcess(String processId, ZonedDateTime time) {
		criteriaBuilder = entityManager.getCriteriaBuilder();
		criteriaQuery = criteriaBuilder.createQuery(entityClass);
		entity = criteriaQuery.from(entityClass);
		
		Predicate processIdEquals = criteriaBuilder.equal(entity.get(Process_.processId), processId);
		Predicate timeGreater = criteriaBuilder.lessThan(entity.get(Process_.startTime), time);
		
		criteriaQuery.where(criteriaBuilder.and(processIdEquals, timeGreater));
		List<Process> entitys = entityManager.createQuery(criteriaQuery).getResultList();
		
		return entitys;
	}
}
