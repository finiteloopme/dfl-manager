package net.dflmngr.model.service;

import java.util.List;

import net.dflmngr.model.entity.InsAndOuts;

public interface InsAndOutsService extends GenericService<InsAndOuts, Integer> {
	public void saveTeamInsAndOuts(List<InsAndOuts> insAndOuts);
	public List<InsAndOuts> getByTeamAndRound(int round, String teamCode);
	public void removeForRound(int round);
}
