package xyz.quazaros.allitems73.items;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.quazaros.allitems73.files.FileHandler;

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
            if (!tempItem.item_stack.isEmpty()) {items.add(tempItem);}
        }

        for (item i : items) {itemMap.put(i.item_name, i);}

        for (itemData d : submit_list) {
            itemMap.get(d.item_name).submit(d.item_founder, d.item_time);
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

    public void updateList(Inventory inv, String player_name) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            String name = Registries.ITEM.getId(stack.getItem()).toString();
            item tempItem = get(name);
            if (tempItem != null && !tempItem.is_found) {
                tempItem.submit(player_name);
            }
        }
    }

    public void updateList(ItemStack itemStack, String player_name) {
        get(itemStack.toString()).submit(player_name);
    }

    public item get(String name) {
        if (name == null) return null;
        String searchName = name.trim().toLowerCase();
        for (item i : items) {
            if (i.item_name.trim().equalsIgnoreCase(searchName)) {
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

    public List<Text> getLeaderboard() {
        List<Text> leaderboard = new ArrayList<>();
        leaderboard.add(Text.literal("Leaderboard").formatted(Formatting.AQUA));

        List<Map.Entry<String, Integer>> leaderboardEntries = getLeaderboardEntries();

        int rank = 1;
        for (Map.Entry<String, Integer> entry : leaderboardEntries) {
            String text = rank++ + ". " +  entry.getKey() + ": " + entry.getValue();
            leaderboard.add(Text.literal(text).formatted(Formatting.LIGHT_PURPLE));
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
