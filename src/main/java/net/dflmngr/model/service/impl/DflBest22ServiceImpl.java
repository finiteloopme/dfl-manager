package net.dflmngr.model.service.impl;

import java.util.List;

import net.dflmngr.model.dao.DflBest22Dao;
import net.dflmngr.model.dao.impl.DflBest22DaoImpl;
import net.dflmngr.model.entity.DflBest22;
import net.dflmngr.model.service.DflBest22Service;

public class DflBest22ServiceImpl extends GenericServiceImpl<DflBest22, Integer> implements DflBest22Service {
	private DflBest22Dao dao;
	
	public DflBest22ServiceImpl() {
		dao = new DflBest22DaoImpl();
		super.setDao(dao);
	}
	
	public List<DflBest22> getForRound(int round) {
		List<DflBest22> best22 = dao.findForRound(round);
		return best22;
	}
}
