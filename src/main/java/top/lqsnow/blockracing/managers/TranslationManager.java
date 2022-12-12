package top.lqsnow.blockracing.managers;

import org.bukkit.Material;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import top.lqsnow.blockracing.Main;

import java.io.*;
import java.util.Objects;

public class TranslationManager {
    public static String getValue(String block) throws IOException, ParseException {
        String key = Objects.requireNonNull(Material.getMaterial(block)).getTranslationKey();
        File file = new File(Main.getInstance().getDataFolder(), "zh_cn.json");
        FileReader reader = new FileReader(file);
        JSONParser parser = new JSONParser();
        Object object = parser.parse(reader);
        JSONObject jsonObject = (JSONObject) object;
        return (String) jsonObject.get(key);
    }
}
