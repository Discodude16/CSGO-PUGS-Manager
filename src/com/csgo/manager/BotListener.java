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
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class BotListener extends ListenerAdapter
{
	static List<Account> accounts = new ArrayList<>(); //All User Accounts
	static Boolean isItemCancellable = false;
	static Timer timer = new Timer();
	
	//All Game Variables
	static List<Account> team1 = new ArrayList<>(); //Game Team 1
	static List<Account> team2 = new ArrayList<>(); //Game Team 2
	static List<Account> players = new ArrayList<>(); //All Registered Players
	static List<String> maps = new ArrayList<String>(); //All Potential Maps
	static Boolean isActive = false; //Is Game Active
	static Boolean inGame = false; //Is The Bot In Game
	static Boolean inTeamCreation = false; //Is In Team Creation
	static Boolean canStartGame = true; //Can We Start a Game
	static Boolean waitingMapVeto = false; //Waiting For Veto to Start
	static Boolean inMapVeto = false; //In Veto
	static Account captain1; //Captain 1
	static Account captain2; //Captain 2
	static Boolean team1Choosing = true; //Team 1 Choosing Map
	
	//All One Vs One Variables
	static Boolean reportingResults = false; //Post Game Reporting Results
	static Boolean onevoneActive = false; //One Vs One Active
	static Boolean playerChallenging = false; //Is A Player Being Challenged
	static String playerBeingChallenged; //The Player Being Challenged
	static Boolean inonevoneGame = false; //In A One Vs One Game
	static Account player1; //Player 1
	static Account player2; //Player 2
	static Boolean postGame = false; //In Post Game
		
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
		Account acc = null;
		for (int i = 0 ; i < accounts.size() ; i++)
		{
			if (accounts.get(i).getId() == id)
			{
				acc = accounts.get(i);
				return acc;
			}
		}
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
	
	public static void adjustMatchesPlayed()
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
	
	public TimerTask cancelChallenge(GuildMessageReceivedEvent e) 
	{
		sayMessage(e.getChannel(), "The Player Did Not Accept In Time! Match Cancelled!");
		resetonevone();
		return null;
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		if (!e.getAuthor().isBot())
		{
			//Register A Player
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
			
			//Get Accounts Profile
			if (e.getMessage().getContentRaw().charAt(0) == '*' && e.getMessage().getContentRaw().charAt(1) == 'p' && e.getMessage().getContentRaw().charAt(2) == 'r' && e.getMessage().getContentRaw().charAt(3) == 'o')
			{
				Boolean usingMentionedAccount = false;
				Account p = null;
				if (e.getMessage().getMentionedMembers().size() >= 1)
				{
					p = getUserById(e.getMessage().getMentionedMembers().get(0).getUser().getIdLong());
					usingMentionedAccount = true;
				}
				else
				{
					p = getUserById(e.getAuthor().getIdLong());
				}
				
				if (p == null)
				{
					if (usingMentionedAccount)
					{
						sayMessage(e.getChannel(), "This User Is Not Registered.");
					}
					else
					{
						sayMessage(e.getChannel(), e.getAuthor().getAsMention() + " - You are not registered! Register with *register");
					}
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
			
			//Cancel An Action
			if (compareMessageRecieved(e, "*Cancel") && isItemCancellable)
			{
				timer.cancel();
				sayMessage(e.getChannel(), "Cancelled Event");
				resetonevone();
				endGame();
				isItemCancellable = false;
			}
			
			//Ask For Help
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
			
			/*---------------------------GAME COMMANDS-------------------*/
			
			//Starts The Game
			if (compareMessageRecieved(e, "*startGame") && hasGamemasterRank(e))
			{
				Game.startGame(e);
			}
			
			//Register A Player
			if (inTeamCreation && e.getMessage().getMentionedMembers().size() != 0)
			{
				Game.registerPlayer(e);
			}
			
			//Starts The Map Veto
			if (compareMessageRecieved(e, "*veto") && waitingMapVeto)
			{
				Game.startMapVeto(e);
			}
			
			//Veto A Map
			if (getMessageAsString(e).charAt(0) == '{' && inMapVeto)
			{
				Game.vetoAMap(e);
			}
			
			//End The Game
			if (compareMessageRecieved(e, "*endGame") && inGame && hasGamemasterRank(e))
			{
				Game.endGame(e);
			}
			
			//Give Team 1 A Win
			if (compareMessageRecieved(e, "*1") && postGame && hasGamemasterRank(e))
			{
				Game.giveTeam1Win(e);
			}
			
			//Give Team 2 A Win
			if (compareMessageRecieved(e, "*2") && postGame && hasGamemasterRank(e))
			{
				Game.giveTeam2Win(e);
			}
			
			/*-------------------------------------------------------------*/
			
			/*-----------------------1V1 COMMANDS----------------------------*/
			
			//Start The One V One Process
			if (compareMessageRecieved(e, "*1v1") && !onevoneActive)
			{
				OneVsOne.start(e);
			}
			
			//Triggers MatchMaking | Called After *1v1
			if (compareMessageRecieved(e, "*Random") && onevoneActive && !playerChallenging && !inonevoneGame && !e.getAuthor().isBot() && getUserById(e.getAuthor().getIdLong()) == player1)
			{
				OneVsOne.matchmakeRandomPlayer(e);
			}
			
			//Challenges A Player And Starts the Timer
			if (e.getMessage().getMentionedMembers().size() == 1 && onevoneActive && !playerChallenging && !inonevoneGame && !e.getAuthor().isBot() && getUserById(e.getAuthor().getIdLong()) == player1) //Challenges A Specific Player
			{
				OneVsOne.challengePlayer(e);
			}
			
			//Accepts A Challenge
			if (compareMessageRecieved(e, "*Yes") && playerChallenging && e.getMessage().getAuthor().getAsMention().equals(playerBeingChallenged))
			{
				OneVsOne.acceptChallenge(e);
			}
			
			//Denies A Challenge
			if (compareMessageRecieved(e, "*No") && playerChallenging && e.getMessage().getAuthor().getAsMention().equals(playerBeingChallenged))
			{
				OneVsOne.denyChallenge(e);
			}
			
			//Starts The Reporting On Who Won
			if (compareMessageRecieved(e, "*endround"))
			{
				reportingResults = true;
				sayMessage(e.getChannel(), "Who Won? Please Mention The User Who Won the Game");
			}
			
			//Ends The Game
			if (e.getMessage().getMentionedMembers().size() == 1 && onevoneActive && !playerChallenging && inonevoneGame && reportingResults)
			{
				OneVsOne.endGame(e);
			}
			
			//Allows Or Disables 1v1
			if (compareMessageRecieved(e, "*Toggle 1v1"))
			{
				OneVsOne.toggleOneVOne(e);
			}
			
			/*-------------------------------------------------------------*/
		}
	}
}