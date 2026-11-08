package top.lqsnow.blockracing.pools;

/** Display/scoring groups only; does not change the generation tiers or pools. */
public enum DisplayDifficulty {
    EASY, MEDIUM, HARD, NIGHTMARE;

    public static DisplayDifficulty forTier(int tier) {
        return switch (tier) {
            case 1, 2 -> EASY;
            case 3 -> MEDIUM;
            case 4, 5 -> HARD;
            case 6 -> NIGHTMARE;
            default -> throw new IllegalArgumentException("Tier must be 1–6");
        };
    }

    public int points(boolean speedMode) { return ordinal() + 1 + (speedMode ? 2 : 0); }
}
