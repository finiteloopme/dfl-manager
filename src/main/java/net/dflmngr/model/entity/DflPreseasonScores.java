package net.dflmngr.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="dfl_preseason_scores")
public class DflPreseasonScores {
	
	@Id
	@Column(name="player_id")
	private int playerId;
	
	@Id
	private int round1;
	
	@Id
	private int round2;
	
	@Id
	private int round3;
	
	@Id
	private int round4;

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getRound1() {
		return round1;
	}

	public void setRound1(int round1) {
		this.round1 = round1;
	}

	public int getRound2() {
		return round2;
	}

	public void setRound2(int round2) {
		this.round2 = round2;
	}

	public int getRound3() {
		return round3;
	}

	public void setRound3(int round3) {
		this.round3 = round3;
	}

	public int getRound4() {
		return round4;
	}

	public void setRound4(int round4) {
		this.round4 = round4;
	}

	@Override
	public String toString() {
		return "DflPreseasonScores [playerId=" + playerId + ", round1=" + round1 + ", round2=" + round2 + ", round3="
				+ round3 + ", round4=" + round4 + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + playerId;
		result = prime * result + round1;
		result = prime * result + round2;
		result = prime * result + round3;
		result = prime * result + round4;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DflPreseasonScores other = (DflPreseasonScores) obj;
		if (playerId != other.playerId)
			return false;
		if (round1 != other.round1)
			return false;
		if (round2 != other.round2)
			return false;
		if (round3 != other.round3)
			return false;
		if (round4 != other.round4)
			return false;
		return true;
	}
}
