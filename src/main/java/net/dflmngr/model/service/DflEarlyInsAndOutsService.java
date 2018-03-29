package net.dflmngr.model.service;

import java.util.List;

import net.dflmngr.model.entity.DflEarlyInsAndOuts;

public interface DflEarlyInsAndOutsService extends GenericService<DflEarlyInsAndOuts, Integer> {
	public List<DflEarlyInsAndOuts> getByTeamAndRound(int round, String teamCode);
	public void saveTeamInsAndOuts(List<DflEarlyInsAndOuts> insAndOuts);
}
