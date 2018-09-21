package com.csgo.manager;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Game extends BotListener
{
	
	public static void startGame(GuildMessageReceivedEvent e)
	{
		if (!inGame && !inTeamCreation && !inonevoneGame)
		{
			inTeamCreation = true;
			isItemCancellable = true;
			sayMessage(e.getChannel(), "Please Begin Entering Team Players. \n To Add A Player, Mention Them. Only Add 1 Player At A Time");
		}
	}
	
	public static void registerPlayer(GuildMessageReceivedEvent e)
	{
		if (players.contains(getUserById(e.getMessage().getMentionedMembers().get(0).getUser().getIdLong())))
		{
			sayMessage(e.getChannel(), "This Player Is Already in the Game!"); 
		}
		else
		{
			if (getUserById(e.getMessage().getMentionedMembers().get(0).getUser().getIdLong()) == null)
			{
				sayMessage(e.getChannel(), "This Player Is Not Registered!"); 
			}
			else
			{
				players.add(getUserById(e.getMessage().getMentionedMembers().get(0).getUser().getIdLong()));
				System.out.println("Added Player - " + getUserById(e.getMessage().getMentionedMembers().get(0).getUser().getIdLong()));
				sayMessage(e.getChannel(), "Added Player - " + (10 - players.size()) + " to go"); 
				if (players.size() == 10)
				{
					sayMessage(e.getChannel(), "All Players Have Been Added! Assigning Teams...");
					setTeams(e);
					
			}
		}
	}
	}
	
	public static void setTeams(GuildMessageReceivedEvent e)
	{
		inTeamCreation = false;
		
		//Make List of All Players in Rank Order
		List<Account> p = new ArrayList<>();
		List<Account> allPlayers = players;
		int currentHighestMMR = 0;
		Account currentHighest;
		for (int i = 1 ; i <= 10 ; i++)
		{
			currentHighestMMR = 0;
			currentHighest = null;
			for (int j = 0 ; j < allPlayers.size() ; j++)
			{
				if (allPlayers.get(j).getMMR() >= currentHighestMMR)
				{
					currentHighestMMR = allPlayers.get(j).getMMR();
					currentHighest = allPlayers.get(j);
				}
			}
			allPlayers.remove(currentHighest);
			p.add(i - 1, currentHighest);
		}
		
		//Make Teams From List
		
		team1.add(0, p.get(0));
		team1.add(1, p.get(2));
		team1.add(2, p.get(4));
		team1.add(3, p.get(6));
		team1.add(4, p.get(8));
		
		team2.add(0, p.get(1));
		team2.add(1, p.get(3));
		team2.add(2, p.get(5));
		team2.add(3, p.get(7));
		team2.add(4, p.get(9));
		
		sayMessage(e.getChannel(), "Teams Have Been Generated!");
		sayMessage(e.getChannel(), "Team 1 - ");
		for (int i = 0 ; i < team1.size() ; i++)
		{
			sayMessage(e.getChannel(), team1.get(i).getName());
		}
		sayMessage(e.getChannel(), "------------------------------");
		sayMessage(e.getChannel(), "Team 2 - ");
		for (int i = 0 ; i < team2.size() ; i++)
		{
			sayMessage(e.getChannel(), team2.get(i).getName());
		}
		sayMessage(e.getChannel(), "------------------------------ \n Please Move to Appropriate Channels. Type *veto to start the map veto!");
		waitingMapVeto = true;
	}
	
	public static void startMapVeto(GuildMessageReceivedEvent e) {
		isItemCancellable = false;
		captain1 = team1.get(0);
		captain2 = team2.get(0);
		
		sayMessage(e.getChannel(), "Team 1 Captain is " + captain1.getName());
		sayMessage(e.getChannel(), "Team 2 Captain is " + captain2.getName());
		
		maps.clear();
		
		maps.add("{Dust2");
		maps.add("{Train");
		maps.add("{Mirage");
		maps.add("{Nuke");
		maps.add("{Overpass");
		maps.add("{Cache");
		maps.add("{Inferno");
		
		sayMessage(e.getChannel(), "Maps Are Dust2, Train, Mirage, Nuke, Overpass, Cache, and Inferno");
		
		sayMessage(e.getChannel(), "Team 1 Captian Choose A Map To Ban - Type {<Map Name> to ban. Ex: {Office");
		inMapVeto = true;
	}
	
	public static void vetoAMap(GuildMessageReceivedEvent e) {
		if (maps.contains(getMessageAsString(e)))
		{
			if ((team1Choosing && e.getAuthor().getIdLong() == captain1.getId()) || (!team1Choosing && e.getAuthor().getIdLong() == captain2.getId()))
			{
				maps.remove((getMessageAsString(e)));
				if (maps.size() == 1)
				{
					sayMessage(e.getChannel(), "Map Veto Is Finished!");
					//Start the Game
					inMapVeto = false;
					inGame = true;
					String mapPlaying = maps.get(0);
					StringBuilder sb = new StringBuilder(mapPlaying);
					sb.deleteCharAt(0);
					mapPlaying = sb.toString();
					sayMessage(e.getChannel(), "Setup Is Complete! \n ------------------ \n Map Playing: " + mapPlaying + " \n Team 2 Chooses Which Side To Start On \n Type *endgame when the match is finished. \n GLHF");
				}
				else
				{
					team1Choosing = !team1Choosing;
					if (!team1Choosing)
						sayMessage(e.getChannel(), "Team 2 Choose a Map to Ban");
					else
						sayMessage(e.getChannel(), "Team 1 Choose a Map to Ban");
				}
			}
			else
			{
				sayMessage(e.getChannel(), "You Are Not Authorized To Ban Maps Yet");
			}
		}
		else
		{
			sayMessage(e.getChannel(), "That is not a valid map (Make sure the cases match)");
		}
	}

	public static void endGame(GuildMessageReceivedEvent e) {
		postGame = true;
		sayMessage(e.getChannel(), "Who Won? (Use *1 or *2)");
	}
	
	public static void giveTeam1Win(GuildMessageReceivedEvent e) {
		sayMessage(e.getChannel(), "Congrats Team 1!");
		sayMessage(e.getChannel(), "Adjusuting Ranks...");
		
		int team1AvregeRank;
		int team2AvregeRank;
		
		int currentMMRAdded1 = 0;
		int currentMMRAdded2 = 0;
		
		List<Integer> team1Ranks = new ArrayList<Integer>();
		List<Integer> team2Ranks = new ArrayList<Integer>();
		for (int i = 0 ; i < team1.size() ; i++)
		{
			team1Ranks.add(team1.get(i).getMMR());
			team2Ranks.add(team2.get(i).getMMR());
		}
		for (int i = 0 ; i < team1.size() ; i++)
		{
			currentMMRAdded1 = currentMMRAdded1 + team1Ranks.get(i); 
			currentMMRAdded2 = currentMMRAdded2 + team2Ranks.get(i); 
		}
		team1AvregeRank = currentMMRAdded1 / team1Ranks.size();
		team2AvregeRank = currentMMRAdded2 / team2Ranks.size();
		
		int mmrDifference = 0;
		
		if (team1AvregeRank > team2AvregeRank)
		{
			mmrDifference = team1AvregeRank - team2AvregeRank;
		}
		else if (team1AvregeRank < team2AvregeRank)
		{
			mmrDifference = team2AvregeRank - team1AvregeRank;
		}
		
		for (int i = 0 ; i < team1.size() ; i++)
		{
			team1.get(i).setMMR((team1.get(i).getMMR() + (mmrDifference + 50)));
			team1.get(i).setMatchesWon(team1.get(i).getMatchesWon() + 1);
		}
		for (int i = 0 ; i < team1.size() ; i++)
		{
			team2.get(i).setMMR((team2.get(i).getMMR() - (mmrDifference - 50)));
		}
		sayMessage(e.getChannel(), "Ranks Have Been Adjusted! GG! \n Ending Game...");
		adjustMatchesPlayed();
		endGame();
	}
	
	public static void giveTeam2Win(GuildMessageReceivedEvent e) {
		sayMessage(e.getChannel(), "Congrats Team 2!");
		sayMessage(e.getChannel(), "Adjusuting Ranks...");
		
		int team1AvregeRank;
		int team2AvregeRank;
		
		int currentMMRAdded1 = 0;
		int currentMMRAdded2 = 0;
		
		List<Integer> team1Ranks = new ArrayList<Integer>();
		List<Integer> team2Ranks = new ArrayList<Integer>();
		for (int i = 0 ; i < team1.size() ; i++)
		{
			team1Ranks.add(team1.get(i).getMMR());
			team2Ranks.add(team2.get(i).getMMR());
		}
		for (int i = 0 ; i < team1.size() ; i++)
		{
			currentMMRAdded1 = currentMMRAdded1 + team1Ranks.get(i); 
			currentMMRAdded2 = currentMMRAdded2 + team2Ranks.get(i); 
		}
		team1AvregeRank = currentMMRAdded1 / team1Ranks.size();
		team2AvregeRank = currentMMRAdded2 / team2Ranks.size();
		
		int mmrDifference = 0;
		
		if (team1AvregeRank > team2AvregeRank)
		{
			mmrDifference = team1AvregeRank - team2AvregeRank;
		}
		else if (team1AvregeRank < team2AvregeRank)
		{
			mmrDifference = team2AvregeRank - team1AvregeRank;
		}
		
		for (int i = 0 ; i < team1.size() ; i++)
		{
			//Team 1 Lost Here
			team1.get(i).setMMR((team1.get(i).getMMR() - (mmrDifference + 50)));
		}
		for (int i = 0 ; i < team1.size() ; i++)
		{
			//Team 2 Won Here
			team2.get(i).setMMR((team2.get(i).getMMR() + (mmrDifference + 50)));
			team2.get(i).setMatchesWon(team2.get(i).getMatchesWon() + 1);
		}
		sayMessage(e.getChannel(), "Ranks Have Been Adjusted! GG! \n Ending Game...");
		adjustMatchesPlayed();
		endGame();
	}
}