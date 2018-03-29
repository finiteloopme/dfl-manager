package net.dflmngr.model.dao;

import java.util.List;

import net.dflmngr.model.entity.DflEarlyInsAndOuts;

public interface DflEarlyInsAndOutsDao extends GenericDao<DflEarlyInsAndOuts, Integer> {
	public List<DflEarlyInsAndOuts> findByTeamAndRound(int round, String teamCode);
}
