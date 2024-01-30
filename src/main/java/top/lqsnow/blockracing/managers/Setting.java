package top.lqsnow.blockracing.managers;

import lombok.Getter;
import lombok.Setter;

public class Setting {
    @Getter
    private static boolean enableMediumBlock;
    @Getter
    private static boolean enableHardBlock;
    @Getter
    private static boolean enableDyedBlock;
    @Getter
    private static boolean enableEndBlock;
    @Getter
    private static int blockAmount;
    @Getter
    private static boolean speedMode;
    public enum GameMode {NORMAL, RACING}
    @Getter
    @Setter
    private static GameMode currentGameMode = GameMode.NORMAL;

    public static void getSettings(){
        enableMediumBlock = Config.MEDIUM_BLOCK.getBoolean();
        enableHardBlock = Config.HARD_BLOCK.getBoolean();
        enableDyedBlock = Config.DYED_BLOCK.getBoolean();
        enableEndBlock = Config.END_BLOCK.getBoolean();
        blockAmount = Config.BLOCK_AMOUNT.getInt();
        speedMode = Config.SPEED_MODE.getBoolean();
        setCurrentGameMode(GameMode.valueOf(Config.GAME_MODE.getString().toUpperCase()));
    }

    public static void setEnableMediumBlock(boolean enableMediumBlock) {
        Setting.enableMediumBlock = enableMediumBlock;
        Config.MEDIUM_BLOCK.setBoolean(enableMediumBlock);
    }

    public static void setEnableHardBlock(boolean enableHardBlock) {
        Setting.enableHardBlock = enableHardBlock;
        Config.HARD_BLOCK.setBoolean(enableHardBlock);
    }

    public static void setEnableDyedBlock(boolean enableDyedBlock) {
        Setting.enableDyedBlock = enableDyedBlock;
        Config.DYED_BLOCK.setBoolean(enableDyedBlock);
    }

    public static void setEnableEndBlock(boolean enableEndBlock) {
        Setting.enableEndBlock = enableEndBlock;
        Config.END_BLOCK.setBoolean(enableEndBlock);
    }

    public static void setBlockAmount(int blockAmount) {
        Setting.blockAmount = blockAmount;
        Config.BLOCK_AMOUNT.setInt(blockAmount);
    }

    public static void setSpeedMode(boolean speedMode) {
        Setting.speedMode = speedMode;
        Config.SPEED_MODE.setBoolean(speedMode);
    }

    public static void toggleMediumBlock() {
        setEnableMediumBlock(!isEnableMediumBlock());
    }

    public static void toggleHardBlock() {
        setEnableHardBlock(!isEnableHardBlock());
    }

    public static void toggleDyedBlock() {
        setEnableDyedBlock(!isEnableDyedBlock());
    }

    public static void toggleEndBlock() {
        setEnableEndBlock(!isEnableEndBlock());
    }

    public static void toggleSpeedMode() {
        setSpeedMode(!isSpeedMode());
    }
}
