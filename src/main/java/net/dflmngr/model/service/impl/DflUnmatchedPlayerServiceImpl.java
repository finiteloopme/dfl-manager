package net.dflmngr.model.service.impl;

import net.dflmngr.model.dao.DflUnmatchedPlayerDao;
import net.dflmngr.model.dao.impl.DflUnmatchedPlayerDaoImpl;
import net.dflmngr.model.entity.DflUnmatchedPlayer;
import net.dflmngr.model.service.DflUnmatchedPlayerService;

public class DflUnmatchedPlayerServiceImpl extends GenericServiceImpl<DflUnmatchedPlayer, Integer> implements DflUnmatchedPlayerService {

	private DflUnmatchedPlayerDao dao;
	
	public DflUnmatchedPlayerServiceImpl() {
		dao = new DflUnmatchedPlayerDaoImpl();
		super.setDao(dao);
	}
	
}
