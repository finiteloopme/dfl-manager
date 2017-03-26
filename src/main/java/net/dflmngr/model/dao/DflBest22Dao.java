package net.dflmngr.model.dao;

import java.util.List;

import net.dflmngr.model.entity.DflBest22;

public interface DflBest22Dao extends GenericDao<DflBest22, Integer> {
	public List<DflBest22> findForRound(int round);
}
