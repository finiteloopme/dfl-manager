package net.dflmngr.model.dao.impl;

import net.dflmngr.model.dao.DflPreseasonScoresDao;
import net.dflmngr.model.entity.DflPreseasonScores;

public class DflPreseasonScoresDaoImpl extends GenericDaoImpl<DflPreseasonScores, Integer> implements DflPreseasonScoresDao {
	
	public DflPreseasonScoresDaoImpl() {
		super(DflPreseasonScores.class);
	}
}
