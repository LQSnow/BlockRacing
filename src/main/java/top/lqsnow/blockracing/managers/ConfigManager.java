package top.lqsnow.blockracing.managers;

import org.bukkit.configuration.file.FileConfiguration;
import top.lqsnow.blockracing.Main;
import static top.lqsnow.blockracing.managers.BlockManager.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    static FileConfiguration config;

    // 读取配置文件
    public ConfigManager() throws IOException {
        easyBlocks = readFile("EasyBlocks.txt");
        normalBlocks = readFile("NormalBlocks.txt");
        hardBlocks = readFile("HardBlocks.txt");
        dyedBlocks = readFile("DyedBlocks.txt");
        endBlocks = readFile("EndBlocks.txt");
    }

    public static String[] readFile(String fileName) throws IOException {
        File file = new File(Main.getInstance().getDataFolder(), fileName);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> lines = new ArrayList<>();
        String line;
        while((line = reader.readLine()) != null) {
            if (!line.equals("")) lines.add(line);
        }
        reader.close();
        return lines.toArray(new String[0]);
    }

    public static FileConfiguration getConfig() {
        return config;
    }
}
