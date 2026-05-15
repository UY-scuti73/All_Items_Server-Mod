package xyz.quazaros.allitems73.items;

public class itemData {
    public String item_name;
    public String item_founder;
    public String item_time;

    public itemData(String item_name, String item_founder, String item_time) {
        this.item_name = item_name;
        this.item_founder = item_founder;
        this.item_time = item_time;
    }

    public String makeString() {
        return item_name + ", " +  item_founder + ", " + item_time;
    }
}
