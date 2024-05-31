package com.example;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;

enum GAMESTATEMACHINE{
    NOGAME, GAMESTART, GAMEEXAMPLE, ONGOING

}

public class GameSession {
    private String playerleader;
    private HashSet<PlayerEntity> playerlist;
    private int start_tick;

    private String gameword;
    private String gameexample;
    private GAMESTATEMACHINE CurrentGameState;

    public GameSession(String playerleader, HashSet<PlayerEntity> playerlist, int start_tick){
        this.playerleader=playerleader;
        this.playerlist=playerlist;
        this.start_tick=start_tick;
        gameword = "";
        gameexample = "";
        CurrentGameState = GAMESTATEMACHINE.NOGAME;
    }

    public boolean playeringame(PlayerEntity name){
        return playerlist.contains(name);
    }
    private void nogame(PlayerEntity sender){
        sender.getServer().getPlayerManager().broadcast(Text.literal(playerleader + " is choosing their word"), false);
        sender.sendMessage(Text.literal("Enter your word: "));
        CurrentGameState = GAMESTATEMACHINE.GAMESTART;
    }
    private void gamestart(PlayerEntity sender, String word){
        sender.getServer().getPlayerManager().broadcast(Text.literal(playerleader + " is providing an example"), false);
        sender.sendMessage(Text.literal("Enter your example: "));
        gameword = word;
        CurrentGameState = GAMESTATEMACHINE.GAMEEXAMPLE;
    }
    private void gameexample(PlayerEntity sender, String word){
        gameexample = word;
        CurrentGameState = GAMESTATEMACHINE.ONGOING;
        sender.getServer().getPlayerManager().broadcast(Text.literal("Guess the word: " + gameexample), false);
    }
    private void gameongoing(PlayerEntity sender, String word){
        String guesser = sender.getName().getString();
        if(word.equalsIgnoreCase(gameword)){
            sender.getServer().getPlayerManager().broadcast(Text.literal(guesser + " got it correct, the word was: " + gameword), false);
            playerleader = guesser;
            nogame(sender);
        }
    }
    public void UpdateGameState(PlayerEntity sender, String word){
        boolean isplayerleader = sender.getName().getString().equalsIgnoreCase(playerleader);
        if(isplayerleader){
            if(CurrentGameState.equals(GAMESTATEMACHINE.NOGAME) && word.equalsIgnoreCase("!start")){
                nogame(sender);
            }
            else if(CurrentGameState.equals(GAMESTATEMACHINE.GAMESTART)){
                gamestart(sender, word);
            }
            else if(CurrentGameState.equals(GAMESTATEMACHINE.GAMEEXAMPLE)){
                gameexample(sender, word);
            }
        }
        else{
            gameongoing(sender, word);
        }
    }
}
