package com.csgo.manager;

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
	
	static Boolean waitingMapVeto = false;
	static Boolean inMapVeto = false;
	static List<String> maps = new ArrayList<String>();
	static Account captain1;
	static Account captain2;
	static Boolean team1Choosing = true;
	
	//So This Was supposed to save all the data, only issue was that it didn't save any of the values in the classes, so this is scrapped for now unless anyone can find a better method
	
	/*
	@SuppressWarnings("unchecked")
	public static void retrieveUsers()
	{
		System.out.println("Retreiving Data...");
		try {
			ObjectInputStream oos = new ObjectInputStream(new FileInputStream("Accounts.dat"));
			try {
				accounts = (List<Account>) oos.readObject();
				oos.close();
				System.out.println("Success! Retrieved " + accounts.size() + " Accounts!");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			System.out.println("Account File Does Not Exsist!");
		}
	}
	*/
	
	/*
	public static void saveUserData()
	{
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Accounts.dat"));
			oos.writeObject(accounts);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
	
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
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		if (!e.getAuthor().isBot())
		{
			if (compareMessageRecieved(e, "*register"))
			{
				if (!accounts.contains(getUserById(e.getAuthor().getIdLong())))
				{
					isActive = true;
					String username = e.getAuthor().getName();
					sayMessage(e.getChannel(), e.getAuthor().getAsMention() + " - Your Account Has Been Created!");
					sayMessage(e.getChannel(), e.getAuthor().getAsMention() + " - Your Username is " + username);
					Account ac = new Account(e.getAuthor().getIdLong());
					ac.setName(e.getAuthor().getName());
					accounts.add(ac);
					//saveUserData();
				}
				else
				{
					sayMessage(e.getChannel(), "This Account is already Registered!");
				}
			}
			if (compareMessageRecieved(e, "*startGame"))
			{
				if (!inGame && !inTeamCreation)
				{
					inTeamCreation = true;
					sayMessage(e.getChannel(), "Please Begin Entering Team 1.");
					sayMessage(e.getChannel(), "To Add A Player, Mention Them. Only Add 1 Player At A Time");
				}
			}
			if (inTeamCreation && e.getMessage().getMentionedMembers().get(0) != null)
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
							List<Account> a = new ArrayList<>();
							List<Account> allAccounts = accounts;
							int currentHighestMMR = 0;
							Account currentHighest;
							for (int i = 1 ; i <= 10 ; i++)
							{
								currentHighestMMR = 0;
								currentHighest = null;
								for (int j = 0 ; j < allAccounts.size() ; j++)
								{
									if (allAccounts.get(j).getMMR() >= currentHighestMMR)
									{
										currentHighestMMR = allAccounts.get(j).getMMR();
										currentHighest = allAccounts.get(j);
									}
								}
								allAccounts.remove(currentHighest);
								a.add(i - 1, currentHighest);
							}
							
							//Make Teams From List
							
							team1.add(0, a.get(0));
							team1.add(1, a.get(2));
							team1.add(2, a.get(4));
							team1.add(3, a.get(6));
							team1.add(4, a.get(8));
							
							team2.add(0, a.get(1));
							team2.add(1, a.get(3));
							team2.add(2, a.get(5));
							team2.add(3, a.get(7));
							team2.add(4, a.get(9));
							
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
							sayMessage(e.getChannel(), "Please Move to Appropriate Channels. Type *veto to start the map veto!");
							waitingMapVeto = true;
					}
				}
			}
			}
			if (compareMessageRecieved(e, "*veto") && waitingMapVeto)
			{
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
							sayMessage(e.getChannel(), "Setup Is Complete!");
							sayMessage(e.getChannel(), "------------------");
							sayMessage(e.getChannel(), "Map Playing: " + maps.get(0));
							sayMessage(e.getChannel(), "Team 2 Chooses the Side to Start");
							sayMessage(e.getChannel(), "GLHF!");
							sayMessage(e.getChannel(), "Type *endgame when the game is finished!");
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
			if (compareMessageRecieved(e, "*endGame"))
			{
				
			}
		}
	}
}
