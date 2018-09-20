package com.csgo.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class OneVsOne extends BotListener
{
	public static void start(GuildMessageReceivedEvent e) {
		if (getUserById(e.getAuthor().getIdLong()) != null)
		{
			isItemCancellable = true;
			onevoneActive = true;
			player1 = getUserById(e.getAuthor().getIdLong());
			sayMessage(e.getChannel(), e.getAuthor().getAsMention() + " Who do you want to challenge? \n Mention Them To Challenge or use *Random to Challenge a Random Online Player");
		}
		else
		{
			sayMessage(e.getChannel(), "You Need To Register In Order To Challenge Someone!");
		}
	}
	
	public static void challengePlayer(GuildMessageReceivedEvent e)
	{
		isItemCancellable = true;
		playerChallenging = true;
		playerBeingChallenged = e.getMessage().getMentionedMembers().get(0).getAsMention();
		sayMessage(e.getChannel(), playerBeingChallenged + " - Do You Accept This Challenge? (Respond with *Yes or *No");
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				resetonevone();
				sayMessage(e.getChannel(), "Player Didn't Respont in Time. Match Cancelled");
			}
		}, 60000);
	}
	
	public static void acceptChallenge(GuildMessageReceivedEvent e)
	{
		timer.cancel();
		isItemCancellable = false;
		player2 = getUserById(e.getAuthor().getIdLong());
		playerChallenging = false;
		playerBeingChallenged = null;
		inonevoneGame = true;
		sayMessage(e.getChannel(), "The Game Has Started \n" + player1.getName() + " vs " + player2.getName() + " \n Game goes to first to ten rounds \n GLHF \n Type *endround to end the game");
	}

	public static void denyChallenge(GuildMessageReceivedEvent e) {
		timer.cancel();
		isItemCancellable = false;
		sayMessage(e.getChannel(), "Challenge Denied! F");
		resetonevone();
	}
	
	public static void matchmakeRandomPlayer(GuildMessageReceivedEvent e)
	{
		List<Account> allAccounts = new ArrayList<>(); //All Online Accounts
		List<Account> potentialOpponents = new ArrayList<>(); //All Potential Opponents
		
		int challengersMMR = getUserById(e.getAuthor().getIdLong()).getSingleMMR(); //MMR Of the player
		
		//Gets All Online Users
		for(int i = 0 ; i < accounts.size() ; i++)
		{
			if (e.getChannel().getMembers().get(i).getOnlineStatus().toString().equals("ONLINE") && e.getChannel().getMembers().get(i) != e.getAuthor() && e.getChannel().getMembers().get(i) != null)
			{
				if (getUserById(e.getChannel().getMembers().get(i).getUser().getIdLong()) != null && getUserById(e.getChannel().getMembers().get(i).getUser().getIdLong()).allowChallenging())
				{
					System.out.println("Found An Online Player");
					allAccounts.add(getUserById(e.getChannel().getMembers().get(i).getUser().getIdLong()));
				}
			}
		}
		System.out.println("Finished Searching For Online Players, Found - " + allAccounts.size());
		if (allAccounts.size() == 0)
		{
			sayMessage(e.getChannel(), "No Players Are Online!");
			return;
		}
		//Fill Potential Accounts
		potentialOpponents = allAccounts;
		System.out.println("Potential Opponents List Filled! Size Is - " + potentialOpponents.size());
		//Checks All MMR and Removes Uneven Accounts
		int maxMMRDifference = 25;
		for (int j = 0 ; j < 3 ; j++)
		{
			System.out.println("Checking MMR with " + maxMMRDifference);
			for(int i = 0 ; i < potentialOpponents.size() ; i++)
			{
				Account challengedPlayer = potentialOpponents.get(i);
				System.out.println("Checking Player " + challengedPlayer.getSingleMMR());
				int challengedMMR = challengedPlayer.getSingleMMR();
				int mmrDifference; //Difference Between MMR
				
				//Calculate MMR Difference
				if (challengedMMR > challengersMMR)
				{
					mmrDifference = challengedMMR - challengersMMR;
				}
				else
				{
					mmrDifference = challengersMMR - challengedMMR;
				}
				
				if (mmrDifference > maxMMRDifference) //25 is the Max MMR Difference To Be Considered For A Match
				{
					potentialOpponents.remove(i);
					System.out.println("Removing Player Due to To High MMR Difference");
				}
			}
			
			if (potentialOpponents.size() > 0) 
			{
				System.out.println("Have Potential List of Players, Breaking");
				break;
			}
			
			if (maxMMRDifference == 75)
			{
				sayMessage(e.getChannel(), "No Potential Opponents Found Within Your Skill Range!");
				resetonevone();
				break;
			}
			System.out.println("No Players Found, Trying Again");
			maxMMRDifference = maxMMRDifference + 25;
		}
		
		System.out.println("Players Have Been Determined.");
		if (potentialOpponents.size() > 0)
		{
			System.out.println("Getting Random Player...");
			//Has 4 Potential Opponents
			Random r = new Random();
			int randomNumber = r.nextInt(potentialOpponents.size());
			System.out.println(randomNumber);
			Account player = potentialOpponents.get(randomNumber);
			System.out.println("Found It!");
			//Finds the User With The Same ID and Messages Them
			for (int i = 0 ; i < allAccounts.size() ; i++)
			{
				User currentUserTesting;
				currentUserTesting = e.getChannel().getMembers().get(i).getUser();
				if (currentUserTesting.getIdLong() == player.getId())
				{
					System.out.println("Found The User That We Were Trying To Reach!");
					sayMessage(e.getChannel() , "Found Player! " + currentUserTesting.getAsMention() + " - Sent a Message To There DMS \n They have 2 minutes to accept");
					currentUserTesting.openPrivateChannel().queue((channel) -> 
					{
						channel.sendMessage(e.getAuthor().getName() + " Has Challenged You To a 1v1. Go to the PUG Discord and Type *Yes To Accept, or *No To Deny.").queue();
						isItemCancellable = true;
						playerChallenging = true;
						playerBeingChallenged = currentUserTesting.getAsMention();
					});
					break;
				}
			}
		}
	}
	
	public static void endGame(GuildMessageReceivedEvent e) {
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
	
	public static void toggleOneVOne(GuildMessageReceivedEvent e) {
		Account accountInFocus = getUserById(e.getAuthor().getIdLong());
		if (accountInFocus.allowChallenging())
		{
			accountInFocus.setallowChallenigng(false);
			sayMessage(e.getChannel(), e.getAuthor().getAsMention() + " - Allow Challenging Has Been Disabled.");
			saveUsers();
		}
		else
		{
			accountInFocus.setallowChallenigng(true);
			sayMessage(e.getChannel(), e.getAuthor().getAsMention() + " - Allow Challenging Has Been Enabled.");
			saveUsers();
		}
	}
}
