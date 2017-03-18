package net.dflmngr.model.service.impl;

import net.dflmngr.model.dao.DflPreseasonScoresDao;
import net.dflmngr.model.dao.impl.DflPreseasonScoresDaoImpl;
import net.dflmngr.model.entity.DflPreseasonScores;
import net.dflmngr.model.service.DflPreseasonScoresService;

public class DflPreseasonScoresServiceImpl extends GenericServiceImpl<DflPreseasonScores, Integer> implements DflPreseasonScoresService {
	
	private DflPreseasonScoresDao dao;
	
	public DflPreseasonScoresServiceImpl() {
		dao = new DflPreseasonScoresDaoImpl();
		super.setDao(dao);
	}
}
