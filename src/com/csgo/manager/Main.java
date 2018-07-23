package com.csgo.manager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

public class Main {

	public static JDA jda;
	public static final String BOT_TOKEN = "";
	
	public static String getToken()
	{
		String token = null;
		
		Properties props = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream("config.properties");
			props.load(input);
			token = props.getProperty("botToken");
		} catch (FileNotFoundException e) {
			System.out.println("Cannot Find Properties File! Finding System Variable");
			token = System.getenv("BOT_TOKEN");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return token;
	}
	
	public static void main(String[] args) {
		try {
			jda = new JDABuilder(AccountType.BOT).addEventListener(new BotListener()).setToken(getToken()).buildBlocking();
			//BotListener.retrieveUsers();
		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
