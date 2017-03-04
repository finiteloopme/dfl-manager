package net.dflmngr.model.service.impl;

import java.util.Map;

import net.dflmngr.model.dao.AflPlayerDao;
import net.dflmngr.model.dao.impl.AflPlayerDaoImpl;
import net.dflmngr.model.entity.AflPlayer;
import net.dflmngr.model.service.AflPlayerService;

public class AflPlayerServiceImpl extends GenericServiceImpl<AflPlayer, String> implements AflPlayerService {
	
	private AflPlayerDao dao;
	
	public AflPlayerServiceImpl() {
		dao = new AflPlayerDaoImpl();
		super.setDao(dao);
	}
	
	public void bulkUpdateDflPlayerId(Map<Integer, AflPlayer> entitys) {
		dao.beginTransaction();
		
		for(Map.Entry<Integer, AflPlayer> entry : entitys.entrySet()) {
		    Integer dflPlayerId = entry.getKey();
		    AflPlayer player = entry.getValue();
		    
		    player.setDflPlayerId(dflPlayerId);
		}
		
		dao.commit();
	}
}
