package co.seoulmate.android.app.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hassanabid on 3/1/16.
 */
public class CategoryUtils {

    public static String getName(int id) {
        return categoryMap().get(id);
    }

    private static Map<Integer,String> categoryMap() {

        Map<Integer,String> catMap = new HashMap<Integer,String>();

        catMap.put(0,"Korean Language");
        catMap.put(1,"News");

        catMap.put(2,"Media & Entertainment");

        catMap.put(3,"Jobs & Internships");
        catMap.put(4,"Scholarships");
        catMap.put(5,"Tourism");
        catMap.put(6,"History & Culture");
        catMap.put(7,"Social");

        return catMap;

    }
}
