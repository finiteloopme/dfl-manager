package net.dflmngr.model.dao.impl;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.Predicate;

//import org.eclipse.persistence.config.QueryHints;

import net.dflmngr.model.dao.ProcessDao;
import net.dflmngr.model.entity.Process;
import net.dflmngr.model.entity.Process_;
//import net.dflmngr.model.entity.keys.ProcessPK;

//public class ProcessDaoImpl extends GenericDaoImpl<Process, ProcessPK> implements ProcessDao {
public class ProcessDaoImpl extends GenericDaoImpl<Process, String> implements ProcessDao {
	public ProcessDaoImpl() {
		super(Process.class);
	}

	public List<Process> findProcessById(String processId) {
		entityManager.clear();
		criteriaBuilder = entityManager.getCriteriaBuilder();
		criteriaQuery = criteriaBuilder.createQuery(entityClass);
		entity = criteriaQuery.from(entityClass);
		
		Predicate processIdEquals = criteriaBuilder.equal(entity.get(Process_.processId), processId);
		
		criteriaQuery.where(processIdEquals);
		TypedQuery<Process> query = entityManager.createQuery(criteriaQuery);
		//query.setHint(QueryHints.QUERY_RESULTS_CACHE, "FALSE");
		//List<Process> entitys = entityManager.createQuery(criteriaQuery).getResultList();
		List<Process> entitys = query.getResultList();
		
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
