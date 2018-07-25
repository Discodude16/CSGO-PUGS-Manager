package com.csgo.manager;

import java.io.Serializable;

public class Account implements Serializable
{
	private static final long serialVersionUID = 7536482295622776147L;

	private long id;
	
	private String name = "";
	private int MMR = 2500;
	private int matchesPlayed = 0;
	private int matchesWon = 0;
	
	public Account(long id)
	{
		this.id = id;
	}
	
	public long getId()
	{
		return id;
	}
	
	public void setId(long id)
	{
		this.id = id;
	}
	
	public int getMMR()
	{
		return MMR;
	}
	
	public void setMMR(int _MMR)
	{
		MMR = _MMR;
	}
	
	public int getMatchesPlayed()
	{
		return matchesPlayed;
	}
	
	public void setMatchesPlayed(int _matchesPlayed)
	{
		matchesPlayed = _matchesPlayed;
	}

	public int getMatchesWon() {
		return matchesWon;
	}

	public void setMatchesWon(int matchesWon) 
	{
		this.matchesWon = matchesWon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
