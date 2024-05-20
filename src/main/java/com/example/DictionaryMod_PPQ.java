package com.example;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public class DictionaryMod_PPQ {
    static int games = 0;
    static Map<Function<Object, Object>, ArrayList<Object>> delayedFuncs = new HashMap<>();
    enum GameStates {
        UNSTARTED,
        MASTERMIND_SETTING_WORD,
        MASTERMIND_SETTING_DESC,
        PLAYERS_GUESSING,
    }
    static GameStates gameState = GameStates.UNSTARTED;
    static int maxTime = 180;   //in secs
    static ServerPlayerEntity mastermind = null;
    static ServerPlayerEntity winner = null;
    static List<ServerPlayerEntity> allButMM;
    static String theWord = "";
    static String theDesc = "";
    static boolean[] revealedLetters;
    static ArrayList<Object> nothingArgs = new ArrayList<>();
    static private String getPlrName(ServerPlayerEntity p) {
        return p.getDisplayName().getString();
    }
    static private void sendMsgToSome(List<ServerPlayerEntity> plrs, String msg) {
        for (ServerPlayerEntity p : plrs) {
            p.sendMessage(Text.literal(msg));
        }
    }
    static private void sendMsgToAll(MinecraftServer s, String msg) {
        List<ServerPlayerEntity> plrs = s.getPlayerManager().getPlayerList();
        sendMsgToSome(plrs, msg);
    }
    //    static void onChatMessage(SignedMessage msg, ServerPlayerEntity plr, MessageType.Parameters params) {
//        MinecraftServer s = plr.getServer();
//        String msgToBeSent = msg.getContent().getString();
//        //useless method LMAO
//    }
    static boolean onMsgSent(SignedMessage msg, ServerPlayerEntity plr, MessageType.Parameters params) {
        MinecraftServer s = plr.getServer();
        String msgToBeSent = msg.getContent().getString();
        LoggerFactory.getLogger("towerknockdown").info("[" + getPlrName(plr) + "] " + msgToBeSent);
        if (gameState != GameStates.UNSTARTED && msgToBeSent.equalsIgnoreCase("!end")) {
            plr.sendMessage(Text.literal("Force-ending the game!"));
            gameState = GameStates.UNSTARTED;
            return false;
        }
        else if (gameState == GameStates.UNSTARTED && (
                msgToBeSent.equalsIgnoreCase("!begin") ||
                        (plr.equals(winner) && msgToBeSent.equalsIgnoreCase("y"))
        )) {
            winner = null;
            games++;
            gameState = GameStates.MASTERMIND_SETTING_WORD;
            sendMsgToAll(s, "Beginning the game!");
            //select random player, let players guess, timer, gradually give hints
            List<ServerPlayerEntity> plrs = s.getPlayerManager().getPlayerList();
            Random rng = new Random();
            mastermind = plrs.get(rng.nextInt(plrs.size()));    //get a random player unless..
            //if
            allButMM = new ArrayList<>();
            for (ServerPlayerEntity p : plrs) {
                if (!p.equals(mastermind))  allButMM.add(p);
            }
            sendMsgToSome(allButMM, "Waiting on " + getPlrName(mastermind) + " to enter their word...");
            plr.sendMessage(Text.literal("You are the mastermind, enter your word:"));
            return false;
        }
        else if (gameState == GameStates.UNSTARTED && msgToBeSent.toLowerCase().contains("!settimelimit")) {
            String[] chunks = msgToBeSent.toLowerCase().split(" ");
            try {
                if (chunks.length == 2) {
                    int proposedMaxTime = Integer.parseInt(chunks[1]);
                    if (proposedMaxTime < 0) {
                        throw new IllegalArgumentException();
                    }
                    maxTime = proposedMaxTime;
                    sendMsgToAll(s, "Time limit has been set to " + maxTime + " seconds.");
                }
            }
            catch (Exception e) {
                plr.sendMessage(Text.literal("Time limit must be a non-negative integer. (Set to 0 for no time limit)"));
            }
            return false;
        }
        else if (gameState == GameStates.UNSTARTED && plr.equals(winner) && msgToBeSent.equalsIgnoreCase("n"))
            sendMsgToAll(s, "Type \"!begin\" to start a new game...");
        else if (gameState == GameStates.MASTERMIND_SETTING_WORD && plr.equals(mastermind)) {
            boolean validWord;
            do {
                theWord = msgToBeSent.strip().toUpperCase();
                revealedLetters = new boolean[theWord.length()];
                revealedLetters[0] = true;
                validWord = !theWord.isEmpty();
                if (!validWord) plr.sendMessage(Text.literal("Invalid word..."));
            } while (!validWord);
            gameState = GameStates.MASTERMIND_SETTING_DESC;
            sendMsgToSome(allButMM, "Waiting on " + getPlrName(mastermind) + " to enter their description...");
            plr.sendMessage(Text.literal("Enter your description:"));
            return false;
        }
        else if (gameState == GameStates.MASTERMIND_SETTING_DESC && plr.equals(mastermind)) {
            theDesc = msgToBeSent.strip();
            gameState = GameStates.PLAYERS_GUESSING;
//            sendMsgToAll(s,"The mastermind, " + getPlrName(mastermind) + ", is ready!");
            sendMsgToAll(s, "The word begins with " + theWord.charAt(0) + " and has " + theWord.length() + " letters...\n" + theDesc);
            if (maxTime > 0)
                sendMsgToAll(s, "You have " + maxTime + " seconds.  BEGIN!!");
            else
                sendMsgToAll(s, "There is no time limit.  BEGIN!!");
            //set timer
            //this looks pretty horrid
            if (nothingArgs.isEmpty()) nothingArgs.add(null);
            int currentGame = games;
            waitFor(s, maxTime * 5/6 * 20, nothingArgs,
                    o -> {
                        if (games == currentGame) {
                            //game ends soon; send warning message
                            sendMsgToAll(s, "There are " + maxTime / 6 + " seconds left to guess!");
                        }
                        return null;
                    }
            );
            waitFor(s, maxTime * 20, nothingArgs,
                    o1 -> {
                        if (games == currentGame && gameState == GameStates.PLAYERS_GUESSING) {
                            //end game if it already hasn't ended
                            gameState = GameStates.UNSTARTED;
                            sendMsgToAll(s, "Time has ran out!\nThe word was: " + theWord);
                        }
                        return null;
                    }
            );
            return false;
        }
        else if (gameState == GameStates.PLAYERS_GUESSING) {
            String[] chunks = msgToBeSent.toLowerCase().split(" ");
            if (plr.equals(mastermind) && chunks[0].equals("!clue")) {
                //give clue
                String hintString = "";
                //for letter clue
                boolean letterRevealed = false;
                if (chunks.length == 2 && chunks[1].equals("next")) {
                    //reveal next letter
                    for (int i = 1; i < theWord.length(); i++) {
                        if (!revealedLetters[i]) {
                            revealedLetters[i] = true;
                            break;
                        }
                    }
                }
                else if (chunks.length == 3 && chunks[1].equals("letter")) {
                    //reveal letter
                    for (int i = 0; i < theWord.length(); i++) {
                        char c = theWord.charAt(i);
                        if (c == chunks[2].toUpperCase().charAt(0)) {
                            revealedLetters[i] = true;
                            letterRevealed = true;
                        }
                    }
                }
                else if (chunks.length == 3 && chunks[1].equals("pos")) {
                    //reveal position
                    int pos = Integer.parseInt(chunks[2]) - 1;
                    if (pos < 0) return false;
                    revealedLetters[Integer.parseInt(chunks[2]) - 1] = true;
                }
                for (int i = 0; i < theWord.length(); i++) {
                    if (revealedLetters[i]) hintString += theWord.charAt(i);
                    else hintString += "_";
                }
                hintString = "HINT: " + hintString;
                if (chunks.length == 3 && chunks[1].equals("letter") && !letterRevealed)
                    hintString += " There is no \"" + chunks[2].toUpperCase() +"\"";
                sendMsgToAll(s, hintString);
                return false;
            }
            else {
                //if correct, end game
                if (msgToBeSent.equalsIgnoreCase(theWord)) {
                    winner = plr;
                    //end game for now; later wait for other players
                    gameState = GameStates.UNSTARTED;
                    sendMsgToAll(s, getPlrName(plr) + " has guessed the word!\nThe word was: " + theWord);
                    ItemStack dia = new ItemStack(Items.DIAMOND);
                    dia.setCustomName(Text.literal("Point"));
                    dia.setCount(1);
                    plr.getInventory().insertStack(dia);
                    //ask if winner wants to be mastermind
                    plr.sendMessage(Text.literal("Do you want to be the mastermind? (y/n)"));
                }
                else {
                    //check if its close (one letter off/missing); if so, privately tell them as such
                    boolean isClose = false;
                    //check one off
                    if (msgToBeSent.length() == theWord.length()) {
                        for (int i = 0; i < theWord.length(); i++) {
                            String repairedGuess = "";
                            for (int j = 0; j < theWord.length(); j++) {
                                if (i == j)
                                    repairedGuess += theWord.charAt(j);
                                else
                                    repairedGuess += msgToBeSent.toUpperCase().charAt(j);
                            }
                            if (repairedGuess.equals(theWord)) {
                                isClose = true;
                                break;
                            }
                        }
                    }
                    //check one missing
                    else if (theWord.length() - msgToBeSent.length() == 1) {
                        for (int i = 0; i < theWord.length(); i++) {
                            String closeToWord = "";
                            for (int j = 0; j < theWord.length(); j++) {
                                if (i != j)
                                    closeToWord += theWord.charAt(j);
                            }
                            if (closeToWord.equals(msgToBeSent)) {
                                isClose = true;
                                break;
                            }
                        }
                    }
                    if (isClose)
                        plr.sendMessage(Text.literal("\"" + msgToBeSent + "\" is close!"));
                }
            }
        }
        return true;
    }

    //goofy workaround of a wait() function
    static void waitFor(MinecraftServer s, int ticks, ArrayList<Object> args, Function<Object, Object> func) {
        int currentTicks = s.getTicks();
        ArrayList<Object> value = new ArrayList<>();
        value.add(currentTicks + ticks);
        value.add(args);
        delayedFuncs.put(func,value);
    }
    static void ticking(MinecraftServer s) {
        int currentTicks = s.getTicks();
        Iterator<Function<Object, Object>> iter = delayedFuncs.keySet().iterator();
        while (iter.hasNext()) {
            Function<Object, Object> func = iter.next();
            if ((int) delayedFuncs.get(func).get(0) <= currentTicks) {
                //execute
                func.apply(delayedFuncs.get(func).get(1));
                iter.remove();
            }
        }
    }
}
