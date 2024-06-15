package com.example;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Random;

public class MultiplicationGame {
    static boolean isGameActive = false;
    private static int displayMultiplication(ServerPlayerEntity sender){
        int num1;
        int num2;
        Random rng = new Random();
        num1 = rng.nextInt(10,100);    //random int from 10-99
        num2 = rng.nextInt(10,100);
        sender.getServer().getPlayerManager().broadcast(Text.literal("Multiply: " + num1 + "*" + num2),false);
        return num1*num2;
    }
    static int answer;
    public static boolean onMsgSent(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) throws NumberFormatException{
        String rawMsg = message.getContent().getString();


        if (rawMsg.equalsIgnoreCase("!start")) {
            isGameActive = true;
            //clear inventories
            for (PlayerEntity p : sender.getServer().getPlayerManager().getPlayerList()){
               p.getInventory().clear();
            }
            answer = displayMultiplication(sender);
        }
        else if (rawMsg.equalsIgnoreCase("!stop")) {
            isGameActive = false;
        }
        else if (isGameActive && Integer.parseInt(rawMsg) == answer) {
            //point system
            ItemStack pt = new ItemStack(Items.DIAMOND);
            pt.setCount(1);
            pt.setCustomName(Text.literal("Point"));
            sender.getInventory().insertStack(pt);
            //Broadcast
            sender.getServer().getPlayerManager().broadcast(Text.literal(sender.getDisplayName().getString() + " answered correctly!  It was " + answer), false);
            answer=displayMultiplication(sender);
        }
        return true;
    }
}
