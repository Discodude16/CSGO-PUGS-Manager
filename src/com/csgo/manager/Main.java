package com.csgo.manager;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

public class Main {

	public static JDA jda;
	public static final String BOT_TOKEN = "NDcwNzI5Nzk0MzA5MzI0ODAx.DjahNg.PDtyjArTHzqnwQQxi1RJPis4Mhs";
	
	public static void main(String[] args) {
		
		try {
			jda = new JDABuilder(AccountType.BOT).addEventListener(new BotListener()).setToken(BOT_TOKEN).buildBlocking();
			//BotListener.retrieveUsers();
		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();
		}
		

	}

}
