package com.example;

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
		currentName = plr.getDisplayName().getString();
		s.sendMessage(Text.literal(plr.getDisplayName().getString() + " sent a message!"));
		if (msg.getContent().getString().equalsIgnoreCase("!begin")) {
			s.sendMessage(Text.literal("begin the game!!!"));
			plr.sendMessage(Text.literal("aaaa"));
		}
	}

	public boolean allowChatMessage(SignedMessage msg, ServerPlayerEntity plr, MessageType.Parameters params) {
		MinecraftServer s = plr.getServer();
		String name = plr.getDisplayName().getString();
		if (currentName == null || !currentName.equals(name))  // let all other messages through
			return true;
		String word = msg.getContent().getString();
		s.sendMessage(Text.literal(name + " picked a string!"));
		s.sendMessage(Text.literal("the string is:" + word));
		currentName = null;
		return false;
	}
}