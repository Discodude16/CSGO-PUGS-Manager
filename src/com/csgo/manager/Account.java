package com.csgo.manager;

import java.io.Serializable;

public class Account implements Serializable
{
	private static final long serialVersionUID = 7536482295622776147L;

	private static long id;
	
	private static String name = "";
	private static int MMR = 0;
	private static int matchesPlayed = 0;
	private static int matchesWon = 0;
	
	public Account(long _id)
	{
		id = _id;
	}
	
	public long getId()
	{
		return id;
	}
	
	public static void setId(long _id)
	{
		_id = id;
	}
	
	public static int getMMR()
	{
		return MMR;
	}
	
	public static void setMMR(int _MMR)
	{
		MMR = _MMR;
	}
	
	public static int getMatchesPlayed()
	{
		return matchesPlayed;
	}
	
	public static void setMatchesPlayed(int _matchesPlayed)
	{
		matchesPlayed = _matchesPlayed;
	}

	public static int getMatchesWon() {
		return matchesWon;
	}

	public static void setMatchesWon(int matchesWon) 
	{
		Account.matchesWon = matchesWon;
	}

	public static String getName() {
		return name;
	}

	public void setName(String name) {
		Account.name = name;
	}
}
