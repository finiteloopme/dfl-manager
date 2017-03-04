package net.dflmngr.model.service;

import java.util.Map;

import net.dflmngr.model.entity.AflPlayer;

public interface AflPlayerService extends GenericService<AflPlayer, String> {
	public void bulkUpdateDflPlayerId(Map<Integer, AflPlayer> entitys);
}
