package com.example;

import java.util.List;
import java.util.Random;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryGame implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("dictionarygame");
	String currentName;
	String word;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!");
		ServerMessageEvents.CHAT_MESSAGE.register(this::onChatMessage);
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(this::allowChatMessage);
	}

	public void onChatMessage(SignedMessage msg, ServerPlayerEntity plr, MessageType.Parameters params) {
		MinecraftServer s = plr.getServer();
		String name = plr.getDisplayName().getString();
		String content = msg.getContent().getString();
		if (this.word == null || this.word == "") {
			if (content.equalsIgnoreCase("!begin")) {
				List<ServerPlayerEntity> players = s.getPlayerManager().getPlayerList();
				this.word = "";
				this.currentName = players.get(new Random().nextInt(players.size())).getDisplayName().getString();
				s.getPlayerManager().broadcast(Text.literal(this.currentName+" will be picking the word!"), false);
			}
		} else {
			if (!name.equals(this.currentName) && content.equalsIgnoreCase(this.word)) {
				s.getPlayerManager().broadcast(Text.literal(name+" got the word!"), false);
				s.getPlayerManager().broadcast(Text.literal("The word was: "+this.word), false);
				s.getPlayerManager().broadcast(Text.literal(this.currentName+" picked the word."), false);
				this.currentName = name;
				this.word = "";
				s.getPlayerManager().broadcast(Text.literal(this.currentName+" will be picking the word!"), false);
			}
		}
	}

	public boolean allowChatMessage(SignedMessage msg, ServerPlayerEntity plr, MessageType.Parameters params) {
		MinecraftServer s = plr.getServer();
		String name = plr.getDisplayName().getString();
		String content = msg.getContent().getString();
		if (this.currentName == null || !this.currentName.equals(name))  // let all other messages through
			return true;
		if (!word.equals(""))  // ignore if we got the secret word
			return true;
		this.word = content.trim();
		s.getPlayerManager().broadcast(Text.literal(this.currentName+" picked a word!"), false);
		s.getPlayerManager().broadcast(Text.literal("Start guessing..."), false);
		return false;
	}
}