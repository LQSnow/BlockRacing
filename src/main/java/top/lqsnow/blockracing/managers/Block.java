package top.lqsnow.blockracing.managers;

import top.lqsnow.blockracing.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Block {
    public static String[] easyBlocks, mediumBlocks, hardBlocks, dyedBlocks, endBlocks, blocks;
    public static List<String> allBlocks = new ArrayList<>();
    public static int maxBlockAmount;

    public Block() throws IOException {
        easyBlocks = readFile("EasyBlocks.txt");
        mediumBlocks = readFile("NormalBlocks.txt");
        hardBlocks = readFile("HardBlocks.txt");
        dyedBlocks = readFile("DyedBlocks.txt");
        endBlocks = readFile("EndBlocks.txt");
    }

    public static void addUpBlocks() {
        allBlocks.clear();
        allBlocks.addAll(Arrays.asList(easyBlocks));
        if (Setting.isEnableMediumBlock()) allBlocks.addAll(Arrays.asList(mediumBlocks));
        if (Setting.isEnableHardBlock()) allBlocks.addAll(Arrays.asList(hardBlocks));
        if (Setting.isEnableDyedBlock()) allBlocks.addAll(Arrays.asList(dyedBlocks));
        if (Setting.isEnableEndBlock()) allBlocks.addAll(Arrays.asList(endBlocks));
        maxBlockAmount = allBlocks.size();
        blocks = allBlocks.toArray(new String[0]);
    }

    public static String[] readFile(String fileName) throws IOException {
        File file = new File(Main.getInstance().getDataFolder(), fileName);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) lines.add(line);
        }
        reader.close();
        return lines.toArray(new String[0]);
    }
}
