package net.dflmngr.model.dao;

import java.util.List;

import net.dflmngr.model.entity.InsAndOuts;

public interface InsAndOutsDao extends GenericDao<InsAndOuts, Integer> {

	public List<InsAndOuts> findByTeamAndRound(int round, String teamCode);
	public List<InsAndOuts> findByRound(int round);
}
