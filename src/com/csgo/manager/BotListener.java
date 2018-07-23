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
				players.add(getUserById(e.getMessage().getMentionedMembers().get(0).getUser().getIdLong()));
				System.out.println("Added Player - " + getUserById(e.getMessage().getMentionedMembers().get(0).getUser().getIdLong()));
				sayMessage(e.getChannel(), "Added Player - " + (10 - players.size()) + " to go"); 
				if (players.size() == 10)
				{
					sayMessage(e.getChannel(), "All Players Have Been Added! Assigning Teams...");
					//TODO: Assign Teams
					inTeamCreation = false;
					
				}
			}
			if (compareMessageRecieved(e, "*endGame"))
			{
				
			}
		}
	}
}
