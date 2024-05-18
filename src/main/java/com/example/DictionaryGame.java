package com.example;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

enum GAMESTATE {
	GAMESTART, GAMEFINISH, GAMEWORD, GAMEEXAMPLE, NOGAME
}

public class DictionaryGame implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("dictionarygame");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!");
		ServerMessageEvents.CHAT_MESSAGE.register(this::onChatMessage);
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(this::onMsgSent);
	}

	public void onChatMessage(SignedMessage msg, ServerPlayerEntity plr, MessageType.Parameters params) {
		MinecraftServer s = plr.getServer();
		s.sendMessage(Text.literal(plr.getDisplayName().getString() + " sent a message!"));
		if (msg.getContent().getString().equalsIgnoreCase("!begin")) {
			s.sendMessage(Text.literal("begin the game!!!"));
			plr.sendMessage(Text.literal("aaaa"));

		}
		plr.getServer().getPlayerManager().broadcast(Text.literal("Te"), true);
	}
	//static boolean GAMEON = true; <-- Wanted to implement a feature where it prevents multiple occurences of the same game

	GAMESTATE state = GAMESTATE.NOGAME;
	String game_word = "";
	public boolean onMsgSent(SignedMessage msg, ServerPlayerEntity plr, MessageType.Parameters params) {
		String msgToBeSent = msg.getContent().getString();

		if(state.equals(GAMESTATE.GAMEEXAMPLE)){

		}

		if (state.equals(GAMESTATE.GAMEWORD)){
			game_word = msg.getContent().getString();
			state = GAMESTATE.GAMEEXAMPLE;
			return false;
		}

		if (msgToBeSent.equalsIgnoreCase("!dict") && !state.equals(GAMESTATE.NOGAME)) {
			state = GAMESTATE.GAMESTART;
			plr.sendMessage(Text.literal("Enter your word:"));
			state = GAMESTATE.GAMEWORD;
			return true;

		}
		else{
			plr.sendMessage(Text.literal("Sorry a game has already started"));
		}

		return true;
	}
}