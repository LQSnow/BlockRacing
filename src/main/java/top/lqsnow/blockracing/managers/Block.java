package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import top.lqsnow.blockracing.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import static top.lqsnow.blockracing.managers.Gui.checkBlockInventory;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public class Block {
    public static List<String> easyBlocks, mediumBlocks, hardBlocks, dyedBlocks, endBlocks, blocks;
    public static List<String> allBlocks = new ArrayList<>();
    public static int maxBlockAmount;
    public static List<String> redTeamBlocks = new ArrayList<>();
    public static List<String> blueTeamBlocks = new ArrayList<>();
    public static List<String> redTeamRemainingBlocks = new ArrayList<>();
    public static List<String> blueTeamRemainingBlocks = new ArrayList<>();

    public Block() {
        easyBlocks = List.of(readFile("EasyBlocks.txt"));
        mediumBlocks = List.of(readFile("MediumBlocks.txt"));
        hardBlocks = List.of(readFile("HardBlocks.txt"));
        dyedBlocks = List.of(readFile("DyedBlocks.txt"));
        endBlocks = List.of(readFile("EndBlocks.txt"));
        addUpBlocks();
    }

    public static void addUpBlocks() {
        allBlocks.clear();
        allBlocks.addAll(List.copyOf(easyBlocks));
        if (Setting.isEnableMediumBlock()) allBlocks.addAll(List.copyOf(mediumBlocks));
        if (Setting.isEnableHardBlock()) allBlocks.addAll(List.copyOf(hardBlocks));
        if (Setting.isEnableDyedBlock()) allBlocks.addAll(List.copyOf(dyedBlocks));
        if (Setting.isEnableEndBlock()) allBlocks.addAll(List.copyOf(endBlocks));
        blocks = List.copyOf(allBlocks);
        maxBlockAmount = blocks.size();
    }

    public static void setupBlocks() {
        if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)) {
            redTeamBlocks = generateBlocks();
            blueTeamBlocks = generateBlocks();
        } else if (Setting.getCurrentGameMode().equals(Setting.GameMode.RACING)) {
            List<String> blocks = generateBlocks();
            redTeamBlocks = List.copyOf(blocks);
            blueTeamBlocks = List.copyOf(blocks);
        }
        redTeamRemainingBlocks.addAll(List.copyOf(redTeamBlocks));
        blueTeamRemainingBlocks.addAll(List.copyOf(blueTeamBlocks));
        Bukkit.getLogger().info("[BlockRacing] Blocks generate complete.");
        Bukkit.getLogger().info("Red team blocks: " + redTeamBlocks.toString());
        Bukkit.getLogger().info("Blue team blocks: " + blueTeamBlocks.toString());
    }

    private static List<String> generateBlocks() {

        addUpBlocks();

        // Create temporary file to generate blocks
        List<String> blocksTemp = new ArrayList<>(blocks);
        List<String> easyTemp = new ArrayList<>(easyBlocks);
        List<String> mediumTemp = new ArrayList<>(mediumBlocks);
        List<String> hardTemp = new ArrayList<>(hardBlocks);
        List<String> dyedTemp = new ArrayList<>(dyedBlocks);
        List<String> endTemp = new ArrayList<>(endBlocks);

        // Choose blocks
        int blockAmount = Setting.getBlockAmount();
        List<String> targetBlocks = new ArrayList<>();

        for (int i = 0; i < blockAmount; i++) {
            // Calculate weights for each difficulty
            int easyWeight = 0;
            int mediumWeight = 0;
            int hardWeight = 0;
            int dyedWeight = 0;
            int endWeight = 0;

            if (easyTemp.size() != 0) easyWeight = calculateEasyBlocksWeight((float) i / blockAmount);

            if (mediumTemp.size() != 0)
                mediumWeight = Setting.isEnableMediumBlock() ? calculateMediumBlocksWeight((float) i / blockAmount) : 0;

            if (hardTemp.size() != 0)
                hardWeight = Setting.isEnableHardBlock() ? calculateHardBlocksWeight((float) i / blockAmount) : 0;

            if (dyedTemp.size() != 0)
                dyedWeight = Setting.isEnableDyedBlock() ? calculateDyedBlocksWeight((float) i / blockAmount) : 0;

            if (endTemp.size() != 0)
                endWeight = Setting.isEnableEndBlock() ? calculateEndBlocksWeight((float) i / blockAmount) : 0;


            // Choose difficulty based on weights
            String difficulty = chooseDifficulty(easyWeight, mediumWeight, hardWeight, dyedWeight, endWeight);

            // Select a block from the corresponding difficulty list
            String selectedBlock = selectBlock(difficulty, easyTemp, mediumTemp, hardTemp, dyedTemp, endTemp);

            // Add the selected block to targetBlocks
            targetBlocks.add(selectedBlock);

            // Remove the selected block from the corresponding difficulty list
            switch (difficulty) {
                case "easy" -> easyTemp.remove(selectedBlock);
                case "medium" -> mediumTemp.remove(selectedBlock);
                case "hard" -> hardTemp.remove(selectedBlock);
                case "dyed" -> dyedTemp.remove(selectedBlock);
                case "end" -> endTemp.remove(selectedBlock);
                default -> throw new IllegalArgumentException("Invalid difficulty");
            }

            // Remove the selected block from the total blocksTemp
            blocksTemp.remove(selectedBlock);
        }

        return targetBlocks;
    }

    // Method to choose difficulty based on weights
    private static String chooseDifficulty(int easyWeight, int mediumWeight, int hardWeight, int dyedWeight, int endWeight) {
        int totalWeight = easyWeight + mediumWeight + hardWeight + dyedWeight + endWeight;
        int randomNumber = new Random().nextInt(totalWeight);

        if (randomNumber < easyWeight) {
            return "easy";
        } else if (randomNumber < easyWeight + mediumWeight) {
            return "medium";
        } else if (randomNumber < easyWeight + mediumWeight + hardWeight) {
            return "hard";
        } else if (randomNumber < easyWeight + mediumWeight + hardWeight + dyedWeight) {
            return "dyed";
        } else {
            return "end";
        }
    }

    // Method to select a block from the corresponding difficulty list
    private static String selectBlock(String difficulty, List<String> easyTemp, List<String> mediumTemp,
                                      List<String> hardTemp, List<String> dyedTemp, List<String> endTemp) {
        return switch (difficulty) {
            case "easy" -> selectRandomBlockFromList(easyTemp);
            case "medium" -> selectRandomBlockFromList(mediumTemp);
            case "hard" -> selectRandomBlockFromList(hardTemp);
            case "dyed" -> selectRandomBlockFromList(dyedTemp);
            case "end" -> selectRandomBlockFromList(endTemp);
            default -> throw new IllegalArgumentException("Invalid difficulty");
        };
    }

    // Method to select a random block from a list
    private static String selectRandomBlockFromList(List<String> blockList) {
        int randomIndex = new Random().nextInt(blockList.size());
        return blockList.get(randomIndex);
    }

    // Calculate weight for easy blocks
    // Weight decreases from 100 to 20 as progress goes from 0 to 1
    public static int calculateEasyBlocksWeight(float progress) {
        return (int) (100 - 80 * progress);
    }

    // Calculate weight for medium blocks
    // Weight increases from 20 to 60 as progress goes from 0 to 0.4,
    // then remains constant at 60 as progress goes from 0.4 to 1
    public static int calculateMediumBlocksWeight(float progress) {
        if (progress <= 0.4) {
            return (int) (20 + 40 * progress / 0.4);
        } else {
            return 60;
        }
    }

    // Calculate weight for hard blocks
    // Weight increases from 1 to 20 as progress goes from 0 to 0.5,
    // then increases from 20 to 60 as progress goes from 0.5 to 1
    public static int calculateHardBlocksWeight(float progress) {
        if (progress <= 0.5) {
            return (int) (1 + 19 * progress / 0.5);
        } else {
            return (int) (20 + 40 * (progress - 0.5) / 0.5);
        }
    }

    // Calculate weight for dyed blocks
    // Weight remains constant at 10 regardless of progress
    public static int calculateDyedBlocksWeight(float progress) {
        return 10;
    }

    // Calculate weight for end blocks
    // Weight is 0 for progress from 0 to 0.8 (Unless only the end block is left),
    // then increases to 60 as progress goes from 0.8 to 1
    public static int calculateEndBlocksWeight(float progress) {
        float prop = (float) endBlocks.size() / blocks.size();
        if (progress <= 1 - prop) {
            if (progress <= 0.8) return 0;
            if (progress > 0.8) return (int) (60 * (progress - 0.8) / 0.2);
        } else {
            return 60;
        }
        return 0;
    }

    // Check if there are any problems with the blocks imported from the file
    public static boolean checkBlock() {
        boolean flag = true;
        for (String str : blocks) {
            try {
                ItemStack item = ItemCreator.of(CompMaterial.fromMaterial(Material.valueOf(str))).amount(64).make();
                checkBlockInventory.setItem(0, item);
            } catch (Exception e) {
                Bukkit.getLogger().severe(String.format("[BlockRacing] " + Message.NOTICE_ERROR_BLOCK.getString(), str));
                sendAll(String.format(Message.NOTICE_ERROR_BLOCK.getString(), str));
                flag = false;
            }
        }
        return flag;
    }

    public static void reloadBlock() {
        easyBlocks = List.of(readFile("EasyBlocks.txt"));
        mediumBlocks = List.of(readFile("MediumBlocks.txt"));
        hardBlocks = List.of(readFile("HardBlocks.txt"));
        dyedBlocks = List.of(readFile("DyedBlocks.txt"));
        endBlocks = List.of(readFile("EndBlocks.txt"));
        addUpBlocks();
    }

    public static String[] readFile(String fileName) {
        try {
            File file = new File(Main.getInstance().getDataFolder(), fileName);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) lines.add(line);
            }
            reader.close();
            return lines.toArray(new String[0]);
        } catch (IOException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "[BlockRacing] Error reading blocks file!", e);
        }
        return null;
    }
}
