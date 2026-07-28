package top.lqsnow.blockracing.managers;

public class Setting {
    private static boolean enableMediumBlock;
    private static boolean enableHardBlock;
    private static boolean enableDyedBlock;
    private static boolean enableEndBlock;
    private static int blockAmount;
    private static int maxTeamChestNum;
    private static  int maxTeamWaypointNum;
    private static int maxRollCount;
    private static boolean speedMode;
    public enum GameMode {NORMAL, RACING}
    private static GameMode currentGameMode = GameMode.NORMAL;

    public static void getSettings(){
        enableMediumBlock = Config.MEDIUM_BLOCK.getBoolean();
        enableHardBlock = Config.HARD_BLOCK.getBoolean();
        enableDyedBlock = Config.DYED_BLOCK.getBoolean();
        enableEndBlock = Config.END_BLOCK.getBoolean();
        blockAmount = Math.max(10, Config.BLOCK_AMOUNT.getInt());
        speedMode = Config.SPEED_MODE.getBoolean();
        maxTeamChestNum = Math.max(1, Math.min(53, Config.MAX_TEAM_CHEST_NUM.getInt()));
        maxTeamWaypointNum = Math.max(1, Math.min(53, Config.MAX_TEAM_WAYPOINT_NUM.getInt()));
        maxRollCount = Math.max(0, Config.MAX_ROLL_COUNT.getInt());
        try {
            setCurrentGameMode(GameMode.valueOf(Config.GAME_MODE.getString().toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException ex) {
            setCurrentGameMode(GameMode.NORMAL);
        }
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

    public static void setMaxTeamChestNum(int chestNum) {
        Setting.maxTeamChestNum = chestNum;
        Config.MAX_TEAM_CHEST_NUM.setInt(chestNum);
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

    public static boolean isEnableMediumBlock() {
        return enableMediumBlock;
    }

    public static boolean isEnableHardBlock() {
        return enableHardBlock;
    }

    public static boolean isEnableDyedBlock() {
        return enableDyedBlock;
    }

    public static boolean isEnableEndBlock() {
        return enableEndBlock;
    }

    public static int getBlockAmount() {
        return blockAmount;
    }

    public static int getMaxTeamChestNum() {
        return maxTeamChestNum;
    }

    public static int getMaxTeamWaypointNum() {
        return maxTeamWaypointNum;
    }

    public static int getMaxRollCount() {
        return maxRollCount;
    }

    public static boolean isSpeedMode() {
        return speedMode;
    }

    public static GameMode getCurrentGameMode() {
        return currentGameMode;
    }

    public static void setCurrentGameMode(GameMode currentGameMode) {
        Setting.currentGameMode = currentGameMode;
        Config.GAME_MODE.setString(currentGameMode.name().toLowerCase());
    }
}
