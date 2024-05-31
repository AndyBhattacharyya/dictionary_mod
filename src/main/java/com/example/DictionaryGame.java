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

import java.sql.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
	HashMap<String, HashSet<PlayerEntity>> gameque = new HashMap<String, HashSet<PlayerEntity>>();
	HashMap<String, GameSession> ActiveGames = new HashMap<String, GameSession>();
	public boolean onMsgSent(SignedMessage msg, ServerPlayerEntity plr, MessageType.Parameters params) {
		//Que Mechanism
		String message = msg.getContent().getString();
		String sender = plr.getDisplayName().getString().toLowerCase();

		if(message.equalsIgnoreCase("!dict") && !gameque.containsKey(sender)){
			HashSet<PlayerEntity> tmp = new HashSet<PlayerEntity>();
			tmp.add(plr);
			gameque.put(sender, tmp) ;
		}
		else if(message.contains("!join")){
			//With key-based data structure, we can easily check whether or not a person exists in the que
			String playerowner = message.split(" ")[1].toLowerCase();
			if(gameque.containsKey(playerowner) && !sender.equals(playerowner)){
				HashSet<PlayerEntity>tmp = gameque.get(playerowner);
				tmp.add(plr);
				gameque.put(playerowner, tmp);
			}
			else {
				plr.sendMessage(Text.literal("Player Does Not Exist"));
			}
		}
		else if(message.equalsIgnoreCase("!start") && gameque.containsKey(sender)){
			//Initialize game session and take it off the que
			HashSet<PlayerEntity> tmp = gameque.get(sender);
			gameque.remove(sender,tmp);
			ActiveGames.put(sender, new GameSession(sender, tmp, plr.getServer().getTicks()));
			plr.getServer().getPlayerManager().broadcast(Text.literal(sender + " started the game"), false);
		}

		//Iterate through all games and check if the player who is sending the message is in the game, then update game state
		for (Map.Entry<String, GameSession> entry: ActiveGames.entrySet()){
			GameSession tmp = entry.getValue();
			if(tmp.playeringame(plr)){
				ActiveGames.get(sender).UpdateGameState(plr, message);
			}
		}
		return false;
	}
}