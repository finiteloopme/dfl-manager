package net.dflmngr.model.service;

import java.util.List;

import net.dflmngr.model.entity.DflBest22;

public interface DflBest22Service extends GenericService<DflBest22, Integer> {
	public List<DflBest22> getForRound(int round) ;
}
