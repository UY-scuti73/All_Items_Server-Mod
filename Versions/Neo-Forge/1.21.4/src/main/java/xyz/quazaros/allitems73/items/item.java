package xyz.quazaros.allitems73.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class item {
    public String item_name;
    public String item_display_name;
    public Item item_type;
    public ItemStack item_stack;
    public String item_founder;
    public String item_time;
    public itemData data;

    public boolean is_found;

    public item(String name) {
        item_name = name;
        item_display_name = camel_case(item_name);
        item_type = getType();
        item_stack = new ItemStack(item_type, 1);

        item_stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);

        is_found = false;

        item_founder = "";
        item_time = "";

        data = getNewData();
    }

    public void submit(String name) {
        is_found = true;
        item_stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        item_founder = name;
        item_time = getCurrentTime();
        data = getNewData();
    }

    public void submit(String name, String time) {
        is_found = true;
        item_stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        item_founder = name;
        item_time = time;
        data = getNewData();
    }

    private Item getType() {
        // NeoForge uses BuiltInRegistries instead of Registries
        try {
            ResourceLocation rl = ResourceLocation.parse(item_name);
            return BuiltInRegistries.ITEM.getValue(rl);
        } catch (Exception e) {
            return Items.AIR;
        }
    }

    private String camel_case(String str) {
        if (str.contains(":")) {
            str = str.substring(str.indexOf(':') + 1);
        }

        StringBuilder sb = new StringBuilder(str.replace('_', ' '));
        for (int i = 0; i < sb.length(); i++) {
            if (i == 0 || sb.charAt(i - 1) == ' ') {
                sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
            }
        }
        return sb.toString();
    }

    private itemData getNewData() {
        return new itemData(item_name, item_founder, item_time);
    }

    private String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        return now.format(formatter);
    }
}