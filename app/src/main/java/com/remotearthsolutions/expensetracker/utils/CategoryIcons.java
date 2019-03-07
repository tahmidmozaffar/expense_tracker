package com.remotearthsolutions.expensetracker.utils;

import com.remotearthsolutions.expensetracker.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryIcons {
    private static Map<String, Integer> icons = new HashMap<String, Integer>() {
        {
            put("cat_bills", R.drawable.cat_bills);
            put("cat_bills1", R.drawable.cat_bills1);
            put("cat_bills2", R.drawable.cat_bills2);
            put("cat_bills3", R.drawable.cat_bills3);
            put("cat_bills4", R.drawable.cat_bills4);
            put("cat_bills5", R.drawable.cat_bills5);
            put("cat_car", R.drawable.cat_car);
            put("cat_car1", R.drawable.cat_car1);
            put("cat_car2", R.drawable.cat_car2);
            put("cat_car3", R.drawable.cat_car3);
            put("cat_car4", R.drawable.cat_car4);
            put("cat_clothes", R.drawable.cat_clothes);
            put("cat_clothes1", R.drawable.cat_clothes1);
            put("cat_clothes2", R.drawable.cat_clothes2);
            put("cat_clothes3", R.drawable.cat_clothes3);
            put("cat_clothes4", R.drawable.cat_clothes4);
            put("cat_clothes5", R.drawable.cat_clothes5);
            put("cat_communication", R.drawable.cat_communication);
            put("cat_communication1", R.drawable.cat_communication1);
            put("cat_communication2", R.drawable.cat_communication2);
            put("cat_communication3", R.drawable.cat_communication3);
            put("cat_communication4", R.drawable.cat_communication4);
            put("cat_communication5", R.drawable.cat_communication5);
            put("cat_eatingout", R.drawable.cat_eatingout);
            put("cat_eatingout1", R.drawable.cat_eatingout1);
            put("cat_eatingout2", R.drawable.cat_eatingout2);
            put("cat_eatingout3", R.drawable.cat_eatingout3);
            put("cat_eatingout4", R.drawable.cat_eatingout4);
            put("cat_eatingout5", R.drawable.cat_eatingout5);
            put("cat_entertainment", R.drawable.cat_entertainment);
            put("cat_entertainment1", R.drawable.cat_entertainment1);
            put("cat_entertainment2", R.drawable.cat_entertainment2);
            put("cat_entertainment3", R.drawable.cat_entertainment3);
            put("cat_entertainment4", R.drawable.cat_entertainment4);
            put("cat_entertainment5", R.drawable.cat_entertainment5);
            put("cat_food", R.drawable.cat_food);
            put("cat_food1", R.drawable.cat_food1);
            put("cat_food2", R.drawable.cat_food2);
            put("cat_food3", R.drawable.cat_food3);
            put("cat_food4", R.drawable.cat_food4);
            put("cat_food5", R.drawable.cat_food5);
            put("cat_food6", R.drawable.cat_food6);
            put("cat_food7", R.drawable.cat_food7);
            put("cat_gift", R.drawable.cat_gift);
            put("cat_gift1", R.drawable.cat_gift1);
            put("cat_gift2", R.drawable.cat_gift2);
            put("cat_gift3", R.drawable.cat_gift3);
            put("cat_gift4", R.drawable.cat_gift4);
            put("cat_gift5", R.drawable.cat_gift5);
            put("cat_health", R.drawable.cat_health);
            put("cat_health1", R.drawable.cat_health1);
            put("cat_health2", R.drawable.cat_health2);
            put("cat_health3", R.drawable.cat_health3);
            put("cat_health4", R.drawable.cat_health4);
            put("cat_health5", R.drawable.cat_health5);
            put("cat_pets", R.drawable.cat_pets);
            put("cat_pets1", R.drawable.cat_pets1);
            put("cat_pets2", R.drawable.cat_pets2);
            put("cat_pets3", R.drawable.cat_pets3);
            put("cat_pets4", R.drawable.cat_pets4);
            put("cat_pets5", R.drawable.cat_pets5);
            put("cat_sports", R.drawable.cat_sports);
            put("cat_sports1", R.drawable.cat_sports1);
            put("cat_sports2", R.drawable.cat_sports2);
            put("cat_sports3", R.drawable.cat_sports3);
            put("cat_sports4", R.drawable.cat_sports4);
            put("cat_sports5", R.drawable.cat_sports5);
            put("cat_taxi", R.drawable.cat_taxi);
            put("cat_taxi1", R.drawable.cat_taxi1);
            put("cat_taxi2", R.drawable.cat_taxi2);
            put("cat_taxi3", R.drawable.cat_taxi3);
            put("cat_taxi4", R.drawable.cat_taxi4);
            put("cat_taxi5", R.drawable.cat_taxi5);
            put("cat_toiletry", R.drawable.cat_toiletry);
            put("cat_toiletry1", R.drawable.cat_toiletry1);
            put("cat_toiletry2", R.drawable.cat_toiletry2);
            put("cat_toiletry3", R.drawable.cat_toiletry3);
            put("cat_toiletry4", R.drawable.cat_toiletry4);
            put("cat_toiletry5", R.drawable.cat_toiletry5);
            put("cat_transport", R.drawable.cat_transport);
            put("cat_transport1", R.drawable.cat_transport1);
            put("cat_transport2", R.drawable.cat_transport2);
            put("cat_transport3", R.drawable.cat_transport3);
            put("cat_transport4", R.drawable.cat_transport4);
            put("cat_transport5", R.drawable.cat_transport5);
            put("cat_family", R.drawable.cat_family);
            put("cat_family1", R.drawable.cat_family1);
            put("cat_family2", R.drawable.cat_family2);
            put("cat_family3", R.drawable.cat_family3);
            put("cat_family4", R.drawable.cat_family4);
            put("cat_family5", R.drawable.cat_family5);
            put("cat_icon1", R.drawable.cat_icon1);
            put("cat_icon2", R.drawable.cat_icon2);
            put("cat_icon3", R.drawable.cat_icon3);
            put("cat_icon4", R.drawable.cat_icon4);
            put("cat_icon5", R.drawable.cat_icon5);
            put("cat_icon6", R.drawable.cat_icon6);
            put("cat_icon7", R.drawable.cat_icon7);
            put("cat_icon8", R.drawable.cat_icon8);
            put("cat_icon9", R.drawable.cat_icon9);
            put("cat_icon10", R.drawable.cat_icon10);
            put("cat_icon11", R.drawable.cat_icon11);
            put("cat_icon12", R.drawable.cat_icon12);
            put("cat_icon13", R.drawable.cat_icon13);
            put("cat_icon14", R.drawable.cat_icon14);
            put("cat_icon15", R.drawable.cat_icon15);
            put("cat_icon16", R.drawable.cat_icon16);
            put("cat_icon17", R.drawable.cat_icon17);
            put("cat_icon18", R.drawable.cat_icon18);
            put("cat_icon19", R.drawable.cat_icon19);
            put("cat_icon20", R.drawable.cat_icon20);
            put("cat_icon21", R.drawable.cat_icon21);
            put("cat_icon22", R.drawable.cat_icon22);
            put("cat_icon23", R.drawable.cat_icon23);
            put("cat_icon24", R.drawable.cat_icon24);
            put("cat_icon25", R.drawable.cat_icon25);
            put("cat_icon26", R.drawable.cat_icon26);
            put("cat_icon27", R.drawable.cat_icon27);
            put("cat_icon28", R.drawable.cat_icon28);
            put("cat_icon29", R.drawable.cat_icon29);
            put("cat_icon30", R.drawable.cat_icon30);
            put("cat_icon31", R.drawable.cat_icon31);
            put("cat_icon32", R.drawable.cat_icon32);
            put("cat_icon33", R.drawable.cat_icon33);
            put("cat_icon34", R.drawable.cat_icon34);
            put("cat_icon35", R.drawable.cat_icon35);
            put("cat_icon36", R.drawable.cat_icon36);
            put("cat_icon37", R.drawable.cat_icon37);
            put("cat_icon38", R.drawable.cat_icon38);
            put("cat_icon39", R.drawable.cat_icon39);
            put("cat_icon40", R.drawable.cat_icon40);
            put("cat_icon41", R.drawable.cat_icon41);
        }
    };

    public static int getIconId(String name) {
        return icons.get(name);
    }

    public static List<String> getAllIcons() {
        return new ArrayList<>(icons.keySet());

    }
}
