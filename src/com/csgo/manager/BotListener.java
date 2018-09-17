package com.csgo.manager;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class BotListener extends ListenerAdapter
{
	
	static List<Account> accounts = new ArrayList<>();
	
	static List<Account> team1 = new ArrayList<>();
	static List<Account> team2 = new ArrayList<>();
	
	static List<Account> players = new ArrayList<>();
	
	static Boolean isActive = false;
	static Boolean inGame = false;
	static Boolean inTeamCreation = false;
	static Boolean canStartGame = true;
	static Boolean waitingMapVeto = false;
	static Boolean inMapVeto = false;
	static List<String> maps = new ArrayList<String>();
	static Account captain1;
	static Account captain2;
	static Boolean team1Choosing = true;
	
	static Boolean reportingResults = false;
	static Boolean onevoneActive = false;
	static Boolean playerChallenging = false;
	static String playerBeingChallenged;
	static Boolean inonevoneGame = false;
	static Account player1;
	static Account player2;
	static Boolean postGame = false;
	
	static Boolean isItemCancellable = false;
	
	public static void retrieveUsers()
	{
		FileInputStream fi;
		try {
			fi = new FileInputStream("accounts.txt");
			ObjectInputStream oi = new ObjectInputStream(fi);
			while (true)
			{
				try {
					accounts.add((Account) oi.readObject());
				} catch (EOFException exc) {
					System.out.println("Reached the End of Accounts");
					break;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Finished! Got " + accounts.size() + " Account");
			oi.close();
			fi.close();
			endGame();
		} catch (FileNotFoundException e) {
			System.out.println("No Accounts File Found!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveUsers()
	{
		try {
			FileOutputStream fo = new FileOutputStream("accounts.txt");
			ObjectOutputStream oo = new ObjectOutputStream(fo);
			
			for (int i = 0 ; i < accounts.size() ; i++)
			{
				oo.writeObject(accounts.get(i));
			}
			oo.close();
			fo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void endGame()
	{
		isActive = false;
		inGame = false;
		inTeamCreation = false;
		canStartGame = true;
		waitingMapVeto = false;
		inMapVeto = false;
		captain1 = null;
		captain2 = null;
		team1Choosing = true;
		postGame = false;
		
		maps.clear();
		team1.clear();
		team2.clear();
		players.clear();
		
		saveUsers();
	}
	
	public static void resetonevone()
	{
		reportingResults = false;
		onevoneActive = false;
		playerChallenging = false;
		playerBeingChallenged = "";
		inonevoneGame = false;
		player1 = null;
		player2 = null;
		postGame = false;
	}
	
	public static String getMessageAsString(GuildMessageReceivedEvent e)
	{
		return e.getMessage().getContentRaw();
	}
	
	public static Account getUserById(long id)
	{
		System.out.println("Checking For " + id + " | Size is " + accounts.size());
		Account acc = null;
		for (int i = 0 ; i < accounts.size() ; i++)
		{
			System.out.println("Current ID Checking - " + accounts.get(i).getId());
			if (accounts.get(i).getId() == id)
			{
				System.out.println("Account Found!");
				acc = accounts.get(i);
				return acc;
			}
		}
		System.out.println("Returning Null for Some Reason");
		return null;
	}
	
	public static Boolean compareMessageRecieved(GuildMessageReceivedEvent e, String compare)
	{
		return e.getMessage().getContentRaw().equalsIgnoreCase(compare);
	}
	
	public static void sayMessage(MessageChannel mc, String text)
	{
		mc.sendMessage(text).queue();
	}
	
	public void adjustMatchesPlayed()
	{
		for (int i = 0 ; i < team1.size() ; i++)
		{
			team1.get(i).setMatchesPlayed(team1.get(i).getMatchesPlayed() + 1);
			team2.get(i).setMatchesPlayed(team2.get(i).getMatchesPlayed() + 1);
		}
	}
	
	public Boolean hasGamemasterRank(GuildMessageReceivedEvent e)
	{
		List<String> roles = new ArrayList<>();
		for (int i = 0 ; i < e.getMember().getRoles().size() ; i++)
		{
			roles.add(e.getMember().getRoles().get(i).getName());
		}
		return roles.contains("Gamemaster");
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		if (!e.getAuthor().isBot())
		{
			if (compareMessageRecieved(e, "*register"))
			{
				if (e.getMessage().getMentionedMembers().size() == 0)
				{
					if (!accounts.contains(getUserById(e.getAuthor().getIdLong())))
					{
						isActive = true;
						sayMessage(e.getChannel(), e.getAuthor().getAsMention() + " - Your Account Has Been Created!");
						Account ac = new Account(e.getAuthor().getIdLong());
						ac.setName(e.getAuthor().getName());
						accounts.add(ac);
						saveUsers();
					}
					else
					{
						sayMessage(e.getChannel(), "This Account is already Registered!");
					}
				}	
			}
			if (compareMessageRecieved(e, "*startGame") && hasGamemasterRank(e))
			{
				if (!inGame && !inTeamCreation && !inonevoneGame)
				{
					inTeamCreation = true;
					isItemCancellable = true;
					sayMessage(e.getChannel(), "Please Begin Entering Team Players. \n To Add A Player, Mention Them. Only Add 1 Player At A Time");
				}
			}
			if (inTeamCreation && e.getMessage().getMentionedMembers().size() != 0)
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
				}
			}
			}
			if (compareMessageRecieved(e, "*veto") && waitingMapVeto)
			{
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
			if (getMessageAsString(e).charAt(0) == '{' && inMapVeto)
			{
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
			if (compareMessageRecieved(e, "*endGame") && inGame && hasGamemasterRank(e))
			{
				postGame = true;
				sayMessage(e.getChannel(), "Who Won? (Use *1 or *2)");
			}
			if (compareMessageRecieved(e, "*1") && postGame && hasGamemasterRank(e))
			{
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
			if (compareMessageRecieved(e, "*2") && postGame && hasGamemasterRank(e))
			{
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
		if (compareMessageRecieved(e, "*profile"))
		{
			Account p = null;
			if (e.getMessage().getMentionedMembers().size() >= 1)
			{
				p = getUserById(e.getMessage().getMentionedMembers().get(0).getUser().getIdLong());
			}
			else
			{
				p = getUserById(e.getAuthor().getIdLong());
			}
			
			if (p == null)
			{
				sayMessage(e.getChannel(), e.getAuthor().getAsMention() + " - You are not registered! Register with *register");
			}
			else
			{
				sayMessage(e.getChannel(), "Player Name: " + p.getName() + "\n" + "Player ID: " + p.getId() + 
						"\n" + "MMR: " + p.getMMR() + 
						"\n" + "One V One MMR: " + p.getSingleMMR() + 
						"\n" + "Matches Won: " + p.getMatchesWon() + " / " + p.getMatchesPlayed() + 
						"\n" + "One V One Matches Won: " + p.getOnevoneWon() + " / " + p.getOnevonePlayed());
			}
		}
		if (compareMessageRecieved(e, "*help"))
		{
			sayMessage(e.getChannel(), e.getAuthor().getAsMention() + " - Sent Help to DMS");
			e.getAuthor().openPrivateChannel().queue((channel) ->
			{
				Help h = new Help();
				String[] helpText = h.help;
				for (int i = 0 ; i < helpText.length ; i++)
				{
					channel.sendMessage(helpText[i]).queue();
				}
			});
		}
		if (compareMessageRecieved(e, "*1v1") && !onevoneActive)
		{
			isItemCancellable = true;
			onevoneActive = true;
			player1 = getUserById(e.getAuthor().getIdLong());
			sayMessage(e.getChannel(), e.getAuthor().getAsMention() + " Who do you want to challenge? \n Mention Them To Challenge");
		}
		if (e.getMessage().getMentionedMembers().size() == 1 && onevoneActive && !playerChallenging && !inonevoneGame && !e.getAuthor().isBot() && getUserById(e.getAuthor().getIdLong()) == player1) //Challenges A Specific Player
		{
			isItemCancellable = true;
			playerChallenging = true;
			playerBeingChallenged = e.getMessage().getMentionedMembers().get(0).getAsMention();
			sayMessage(e.getChannel(), playerBeingChallenged + " - Do You Accept This Challenge? (Respond with *Yes or *No");
		}
		if (compareMessageRecieved(e, "*Yes") && playerChallenging && e.getMessage().getAuthor().getAsMention().equals(playerBeingChallenged))
		{
			isItemCancellable = false;
			player2 = getUserById(e.getAuthor().getIdLong());
			playerChallenging = false;
			playerBeingChallenged = null;
			inonevoneGame = true;
			sayMessage(e.getChannel(), "The Game Has Started \n" + player1.getName() + " vs " + player2.getName() + " \n Game goes to first to ten rounds \n GLHF \n Type *endround to end the game");
		}
		if (compareMessageRecieved(e, "*No") && playerChallenging && e.getMessage().getAuthor().getAsMention().equals(playerBeingChallenged))
		{
			isItemCancellable = false;
			sayMessage(e.getChannel(), "Challenge Denied! F");
		}
		if (compareMessageRecieved(e, "*endround"))
		{
			reportingResults = true;
			sayMessage(e.getChannel(), "Who Won? Please Mention The User Who Won the Game");
		}
		if (e.getMessage().getMentionedMembers().size() == 1 && onevoneActive && !playerChallenging && inonevoneGame && reportingResults)
		{
			Account winner = null;
			Account loser = null;
			if (player1.getId() == e.getMessage().getMentionedMembers().get(0).getUser().getIdLong())
			{
				winner = player1;
				loser = player2;
			}
			else
			{
				winner = player2;
				loser = player1;
			}
			sayMessage(e.getChannel(), "Congrats " + winner.getName());
			
			//Get MMR To Change
			int mmrToChange;
			
			//Calc the difference
			System.out.println("Winner - " + winner.getSingleMMR() + "Loser - " + loser.getSingleMMR());
			if (winner.getSingleMMR() <= loser.getSingleMMR())
			{
				System.out.println("Using Winner - Loser MMR");
				mmrToChange = loser.getSingleMMR() - winner.getSingleMMR();
				mmrToChange = mmrToChange / 4;
				if (mmrToChange <= 25)
				{
					mmrToChange = 25;
				}
			}
			else
			{
				System.out.println("Using Loser - Winner MMR");
				mmrToChange = Math.abs(loser.getSingleMMR() - winner.getSingleMMR());
				if (mmrToChange > 100)
				{
					mmrToChange = 25;
				}
			}
			
			winner.setSingleMMR(winner.getSingleMMR() + mmrToChange);
			winner.setOnevoneWon(winner.getOnevoneWon() + 1);
			winner.setOnevonePlayed(winner.getOnevonePlayed() + 1);
			loser.setSingleMMR(loser.getSingleMMR() - mmrToChange);
			loser.setOnevonePlayed(loser.getOnevonePlayed() + 1);
			sayMessage(e.getChannel(), "Game Over. MMR Has Been Adjusted by " + mmrToChange + " MMR. Profiles Have Been Saved!");
			saveUsers();
			resetonevone();
		}
		if (compareMessageRecieved(e, "*Cancel") && isItemCancellable)
		{
			sayMessage(e.getChannel(), "Cancelled Event");
			resetonevone();
			endGame();
			isItemCancellable = false;
		}
		
	}
}
