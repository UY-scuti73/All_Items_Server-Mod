package xyz.quazaros.allitems73.items;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import xyz.quazaros.allitems73.files.FileHandler;
import xyz.quazaros.allitems73.network.ItemDataPayloadEntry;

import java.util.*;
import java.util.stream.Collectors;

public class itemList {
    public ArrayList<item> items;
    public Map<String, item> itemMap;

    public itemList() {
        items = new ArrayList<>();
        itemMap = new HashMap<>();

        ArrayList<String> string_list = (ArrayList<String>) FileHandler.getBaseItemList();
        ArrayList<itemData> submit_list = FileHandler.getItemData();

        for (String s : string_list) {
            item tempItem = new item(s);

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

    public void initializeFromNames(List<String> names) {
        this.items.clear();
        for (String name : names) {
            item tempItem = new item(name);
            if (tempItem.item_stack != null && !tempItem.item_stack.isEmpty()) {
                items.add(tempItem);
            }
        }
    }

    public int getSize() {
        return items.size();
    }

    public void updateList(Container inv, String founderName) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

            item tempItem = this.get(itemName);

            if (tempItem != null) {
                if (!tempItem.is_found) {
                    tempItem.submit(founderName);
                }
            }
        }
    }

    public void updateList(ItemStack itemStack, String player_name) {
        if (itemStack.isEmpty()) return;
        get(itemStack.getItemHolder().getRegisteredName()).submit(player_name);
    }

    public item get(String name) {
        if (name == null) return null;
        String searchName = name.trim().toLowerCase();

        for (item i : this.items) {
            if (i.data.item_name.trim().equalsIgnoreCase(searchName)) {
                return i;
            }
        }
        return null;
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