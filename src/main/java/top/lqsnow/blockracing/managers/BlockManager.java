package top.lqsnow.blockracing.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static top.lqsnow.blockracing.listeners.InventoryEventListener.*;

public class BlockManager {
    // 设置方块库
    public static int maxBlockAmount;
    public static String[] easyBlocks, normalBlocks, hardBlocks, dyedBlocks, endBlocks, blocks;
    public static List<String> var = new ArrayList<>();

    public static void init() {
        var.clear();
        var.addAll(Arrays.asList(easyBlocks));
        if (enableNormalBlock) var.addAll(Arrays.asList(normalBlocks));
        if (enableHardBlock) var.addAll(Arrays.asList(hardBlocks));
        if (enableDyedBlock) var.addAll(Arrays.asList(dyedBlocks));
        if (enableEndBlock) var.addAll(Arrays.asList(endBlocks));
        maxBlockAmount = var.size();
        blocks = var.toArray(new String[0]);
    }
}
