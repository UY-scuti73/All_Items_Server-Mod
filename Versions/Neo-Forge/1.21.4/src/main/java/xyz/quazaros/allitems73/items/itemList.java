package xyz.quazaros.allitems73.items;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import xyz.quazaros.allitems73.files.FileHandler;

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
            // NeoForge item checks
            if (tempItem.item_stack != null && !tempItem.item_stack.isEmpty()) {
                items.add(tempItem);
            }
        }

        for (item i : items) {
            itemMap.put(i.item_name, i);
        }

        for (itemData d : submit_list) {
            item target = itemMap.get(d.item_name);
            if (target != null) {
                target.submit(d.item_founder, d.item_time);
            }
        }
    }

    public int getSize() {
        return items.size();
    }

    public void updateList(Container inv, String player_name) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            // In 1.21.1, use the item registry name for the key
            String name = stack.getItemHolder().getRegisteredName();
            item tempItem = get(name);
            if (!tempItem.is_found) {
                tempItem.submit(player_name);
            }
        }
    }

    public void updateList(ItemStack itemStack, String player_name) {
        if (itemStack.isEmpty()) return;
        get(itemStack.getItemHolder().getRegisteredName()).submit(player_name);
    }

    public item get(String name) {
        item tempItem = itemMap.get(name);
        if (tempItem == null) {
            return new item("minecraft:air");
        }
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
        // Priority: Not found items first, then found items
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
        leaderboard.add(Component.literal("Leaderboard").withStyle(ChatFormatting.AQUA));

        List<Map.Entry<String, Integer>> leaderboardEntries = getLeaderboardEntries();

        int rank = 1;
        for (Map.Entry<String, Integer> entry : leaderboardEntries) {
            String text = rank + ". " + entry.getKey() + ": " + entry.getValue();
            leaderboard.add(Component.literal(text).withStyle(ChatFormatting.LIGHT_PURPLE));
            rank++;
        }

        return leaderboard;
    }

    List<Map.Entry<String, Integer>> getLeaderboardEntries() {
        Map<String, Integer> leaderboard = new HashMap<>();
        for (item i : items) {
            if (i.is_found) {
                leaderboard.put(i.item_founder, leaderboard.getOrDefault(i.item_founder, 0) + 1);
            }
        }

        return leaderboard.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());
    }
}