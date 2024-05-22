package com.example;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

enum GAMESTATE {
	GAMESTART, GAMEEXAMPLE, NOGAME, ONGOING
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
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(this::onMsgSent);
	}


	GAMESTATE state = GAMESTATE.NOGAME;
	String initplayername = null;
	String gameword = "";
	String gameexamlpe ="";

	public boolean onMsgSent(SignedMessage msg, ServerPlayerEntity plr, MessageType.Parameters params) {
		String message = msg.getContent().getString();
		switch(state){
			case NOGAME:
				if(message.equalsIgnoreCase("!dict")){
					initplayername = plr.getDisplayName().getString();
					plr.getServer().getPlayerManager().broadcast(Text.literal("It is now "+initplayername+" turn"), false);
					plr.sendMessage(Text.literal("Enter your word:"));
					state = GAMESTATE.GAMESTART;
					return false;
				}
			case GAMESTART:
				if(plr.getDisplayName().getString().equals(initplayername)){
					gameword = message;
					plr.sendMessage(Text.literal("Enter your hint:"));
					state = GAMESTATE.GAMEEXAMPLE;
					return false;
				}
			case GAMEEXAMPLE:
				if(plr.getDisplayName().getString().equals(initplayername)) {
					gameexamlpe = message;
					plr.getServer().getPlayerManager().broadcast(Text.literal(initplayername + " Hint: " + gameexamlpe), false);
					return false;
				}
			case ONGOING:
				if(!plr.getDisplayName().getString().equals(initplayername)){
					if(message.equalsIgnoreCase(gameword)){
						String tmpname = plr.getDisplayName().getString();
						plr.getServer().getPlayerManager().broadcast(Text.literal(tmpname+ " Got it Correct: " + gameword), false);
						plr.getServer().getPlayerManager().broadcast(Text.literal("It is now "+tmpname+" turn"), false);
						plr.sendMessage(Text.literal("Enter your word:"));
						state = GAMESTATE.GAMESTART;
						initplayername = tmpname;
					}

				}

		}
	return true;
	}
}