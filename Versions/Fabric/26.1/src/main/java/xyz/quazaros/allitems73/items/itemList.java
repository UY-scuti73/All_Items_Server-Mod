package xyz.quazaros.allitems73.items;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import xyz.quazaros.allitems73.files.FileHandler;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.stream.Collectors;

public class itemList {
    public ArrayList<item> items;
    public Map<String, item> itemMap;

    public itemList() {
        items = new ArrayList<>();
        itemMap = new HashMap<>();

        ArrayList<String> string_list = FileHandler.getBaseItemList();
        ArrayList<itemData> submit_list = FileHandler.getItemData();

        for (String s : string_list) {
            item tempItem = new item(s);
            if (!tempItem.item_stack.isEmpty()) {items.add(tempItem);}
        }

        for (item i : items) {itemMap.put(i.item_name, i);}

        for (itemData d : submit_list) {
            itemMap.get(d.item_name).submit(d.item_founder, d.item_time);
        }
    }

    public int getSize() {
        return items.size();
    }

    public void updateList(Inventory inv, String player_name) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            String name = inv.getItem(i).getItem().toString();
            item tempItem = get(name);
            if (!tempItem.is_found) {
                tempItem.submit(player_name);
            }
        }
    }

    public void updateList(ItemStack itemStack, String player_name) {
        get(itemStack.toString()).submit(player_name);
    }

    public item get(String name) {
        item tempItem = itemMap.get(name);
        if (tempItem == null) {return new item("minecraft:air");}
        return tempItem;
    }

    public String getProgString() {
        int score = 0;
        for (item i : items) {
            if (i.is_found) {
                score++;
            }
        }

        return score + "/" + items.size();
    }

    public ArrayList<item> getFilteredList() {
        ArrayList<item> filteredList = new ArrayList<>();
        for (item i : items) {
            if (!i.is_found && !i.item_stack.isEmpty()) {
                filteredList.add(i);
            }
        }
        for (item i : items) {
            if (i.is_found && !i.item_stack.isEmpty()) {
                filteredList.add(i);
            }
        }
        return filteredList;
    }

    public List<Component> getLeaderboard() {
        List<Component> leaderboard = new ArrayList<>();
        leaderboard.add(Component.literal("Leaderboard").withStyle(ChatFormatting.LIGHT_PURPLE));

        List<Map.Entry<String, Integer>> leaderboardEntries = getLeaderboardEntries();

        for (Map.Entry<String, Integer> entry : leaderboardEntries) {
            String text = "1. " +  entry.getKey() + ": " + entry.getValue();
            leaderboard.add(Component.literal(text).withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        return leaderboard;
    }

    List<Map.Entry<String, Integer>> getLeaderboardEntries() {
        Map<String, Integer> leaderboard = new HashMap<>();
        for (item i : items) {
            if (i.is_found) {
                if (leaderboard.containsKey(i.item_founder)) {
                    leaderboard.replace(i.item_founder, leaderboard.get(i.item_founder) + 1);
                } else {
                    leaderboard.put(i.item_founder, 1);
                }
            }
        }

        List<Map.Entry<String, Integer>> leaderboardEntries = leaderboard.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        return leaderboardEntries;
    }
}
