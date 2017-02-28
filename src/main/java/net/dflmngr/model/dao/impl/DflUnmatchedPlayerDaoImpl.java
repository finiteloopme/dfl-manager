package net.dflmngr.model.dao.impl;

import net.dflmngr.model.dao.DflUnmatchedPlayerDao;
import net.dflmngr.model.entity.DflUnmatchedPlayer;

public class DflUnmatchedPlayerDaoImpl extends GenericDaoImpl<DflUnmatchedPlayer, Integer> implements DflUnmatchedPlayerDao {
	
	public DflUnmatchedPlayerDaoImpl() {
		super(DflUnmatchedPlayer.class);
	}

}
