package top.lqsnow.blockracing.pools;

import org.bukkit.configuration.ConfigurationSection;
import java.util.*;

/** Immutable match rules with independent team state and a deterministic lazy Racing sequence. */
public final class TaskSession {
    private final DifficultyPreset preset;
    private final boolean copperExtras;
    private final int total;
    private final int maxRolls;
    private final boolean racing;
    private final long seed;
    private final List<PoolEntry> pool;
    private final Map<String, PoolEntry> indexed = new HashMap<>();
    private final TeamState red = new TeamState();
    private final TeamState blue = new TeamState();
    private final List<String> redSequence = new ArrayList<>();
    private final List<String> blueSequence = new ArrayList<>();

    private static final class TeamState {
        final List<String> active = new ArrayList<>();
        final List<String> completed = new ArrayList<>();
        final Set<String> seen = new LinkedHashSet<>();
        int generated;
        int rolls;
    }

    public TaskSession(DifficultyPreset preset, boolean copperExtras, int total, int maxRolls,
                       boolean racing, long seed, List<PoolEntry> catalog) {
        this(preset, copperExtras, total, maxRolls, racing, seed, catalog, true);
    }

    private TaskSession(DifficultyPreset preset, boolean copperExtras, int total, int maxRolls,
                        boolean racing, long seed, List<PoolEntry> catalog, boolean initialize) {
        if (total < 1 || total > 100_000 || maxRolls < 0) throw new IllegalArgumentException("Invalid match target/roll count");
        this.preset = Objects.requireNonNull(preset);
        this.copperExtras = copperExtras;
        this.total = total;
        this.maxRolls = maxRolls;
        this.racing = racing;
        this.seed = seed;
        pool = catalog.stream().filter(preset::allows).filter(e -> copperExtras || !e.copperExtra())
                .sorted(Comparator.comparing(PoolEntry::id)).toList();
        for (PoolEntry entry : pool) {
            if (indexed.put(entry.id(), entry) != null) throw new IllegalArgumentException("Duplicate target: " + entry.id());
            if (preset.pacing().enabled() && entry.acquisition() == null) {
                throw new IllegalArgumentException("Missing acquisition tag: " + entry.id());
            }
        }
        if (initialize) {
            validateCapacity(new TeamState(), 0);
            fillInitial("red");
            fillInitial("blue");
        }
    }

    public DifficultyPreset preset() { return preset; }
    public boolean copperExtras() { return copperExtras; }
    public int total() { return total; }
    public int maxRolls() { return maxRolls; }
    public boolean racing() { return racing; }
    public List<PoolEntry> pool() { return pool; }
    public PoolEntry find(String id) { return indexed.get(id); }
    public List<String> active(String team) { return List.copyOf(state(team).active); }
    public List<String> seen(String team) { return List.copyOf(state(team).seen); }
    public int completed(String team) { return state(team).completed.size(); }
    public int rolls(String team) { return state(team).rolls; }
    public int rollsRemaining(String team) { return Math.max(0, maxRolls - state(team).rolls); }
    public int generated(String team) { return state(team).generated; }

    private TeamState state(String team) {
        return switch (team) { case "red" -> red; case "blue" -> blue; default -> throw new IllegalArgumentException("Unknown team: " + team); };
    }

    private void fillInitial(String team) {
        TeamState state = state(team);
        while (state.generated < Math.min(4, total)) {
            String id = baseAt(team, state.generated++, 0);
            state.active.add(id);
            state.seen.add(id);
        }
    }

    private long streamSeed(String team, int ordinal, long branch) {
        long side = racing ? 0 : team.equals("red") ? 0x51ED270BL : 0x165667B1L;
        return seed ^ side ^ (0x9E3779B97F4A7C15L * (ordinal + 1L)) ^ (0xC2B2AE3D27D4EB4FL * branch);
    }

    private String baseAt(String team, int ordinal, int completed) {
        List<String> sequence = racing || team.equals("red") ? redSequence : blueSequence;
        if (sequence.size() < ordinal) throw new IllegalStateException("Missing saved task sequence prefix");
        if (sequence.size() == ordinal) {
            // Use the team's actual completed progress at refill, including after rolls/restoration.
            sequence.add(draw(completed, new HashSet<>(sequence), new Random(streamSeed(team, ordinal, 0))));
        }
        return sequence.get(ordinal);
    }

    public boolean complete(String team, String id) {
        TeamState state = state(team);
        if (!state.active.contains(id)) return false;
        String next = null;
        if (state.generated < total) {
            next = baseAt(team, state.generated, state.completed.size() + 1);
            // A team roll creates a branch. Resolve a future collision without repeating a seen target.
            if (state.seen.contains(next)) {
                next = draw(state.completed.size() + 1, state.seen,
                        new Random(streamSeed(team, state.generated, state.rolls + 1L)));
            }
        }
        state.active.remove(id);
        state.completed.add(id);
        if (next != null) {
            state.active.add(next);
            state.seen.add(next);
            state.generated++;
        }
        return true;
    }

    /** Generate the whole replacement group and check future capacity before mutating anything. */
    public boolean roll(String team) {
        TeamState state = state(team);
        if (state.rolls >= maxRolls || state.active.isEmpty()) return false;
        TeamState proposed = new TeamState();
        proposed.generated = state.generated;
        proposed.completed.addAll(state.completed);
        proposed.seen.addAll(state.seen);
        try {
            Random random = new Random(streamSeed(team, state.generated, state.rolls + 1L + state.seen.size()));
            for (int i = 0; i < state.active.size(); i++) {
                String replacement = draw(state.completed.size(), proposed.seen, random);
                proposed.active.add(replacement);
                proposed.seen.add(replacement);
            }
            validateCapacity(proposed, state.completed.size());
        } catch (IllegalArgumentException | IllegalStateException exhausted) {
            return false;
        }
        state.active.clear();
        state.active.addAll(proposed.active);
        state.seen.addAll(proposed.seen);
        state.rolls++;
        return true;
    }

    private void validateCapacity(TeamState state, int startCompleted) {
        for (int completed = startCompleted; completed < total; completed++) {
            int count = completed;
            long unseen = pool.stream().filter(e -> preset.unlocked(e, count, total))
                    .filter(e -> !state.seen.contains(e.id())).count();
            int needed = Math.min(total, completed + 4) - state.generated;
            if (unseen < needed) {
                throw new IllegalArgumentException("Insufficient eligible targets at completed=" + completed
                        + ": need " + needed + ", available " + unseen);
            }
        }
    }

    private String draw(int completed, Set<String> excluded, Random random) {
        List<PoolEntry> eligible = pool.stream().filter(e -> preset.unlocked(e, completed, total))
                .filter(e -> !excluded.contains(e.id())).toList();
        if (eligible.isEmpty()) throw new IllegalStateException("No eligible targets at completed=" + completed);
        List<PoolEntry.Dimension> dimensions = Arrays.stream(PoolEntry.Dimension.values())
                .filter(d -> eligible.stream().anyMatch(e -> e.dimension() == d)).toList();
        PoolEntry.Dimension dimension = weighted(dimensions, d -> preset.dimensionWeights().get(d.ordinal()), random);
        List<Integer> tiers = eligible.stream().filter(e -> e.dimension() == dimension).map(PoolEntry::tier).distinct().sorted().toList();
        int tier = weighted(tiers, t -> preset.tierWeight(t, completed, total), random);
        List<PoolEntry> layer = eligible.stream().filter(e -> e.dimension() == dimension && e.tier() == tier).toList();
        if (preset.pacing().enabled()) {
            List<Integer> weights = preset.pacing().weightsAt(completed, total);
            List<PoolEntry.Acquisition> categories = Arrays.stream(PoolEntry.Acquisition.values())
                    .filter(category -> layer.stream().anyMatch(e -> e.acquisition() == category)).toList();
            PoolEntry.Acquisition category = weighted(categories, c -> weights.get(c.ordinal()), random);
            List<PoolEntry> candidates = layer.stream().filter(e -> e.acquisition() == category).toList();
            return candidates.get(random.nextInt(candidates.size())).id();
        }
        // Preserve the exact old RNG call sequence for matches started before category pacing existed.
        return layer.get(random.nextInt(layer.size())).id();
    }

    private static <T> T weighted(List<T> choices, java.util.function.ToIntFunction<T> weight, Random random) {
        int value = random.nextInt(choices.stream().mapToInt(weight).sum());
        for (T choice : choices) {
            value -= weight.applyAsInt(choice);
            if (value < 0) return choice;
        }
        throw new IllegalStateException("Invalid weights");
    }

    public void save(ConfigurationSection section) {
        section.set("generation-rules-version", 2);
        preset.save(section.createSection("preset"));
        section.set("copper-extras", copperExtras);
        section.set("total", total);
        section.set("max-rolls", maxRolls);
        section.set("racing", racing);
        section.set("seed", seed);
        section.set("pool", pool.stream().map(PoolEntry::encode).toList());
        section.set("sequences.red", List.copyOf(redSequence));
        section.set("sequences.blue", List.copyOf(blueSequence));
        saveTeam(section.createSection("red"), red);
        saveTeam(section.createSection("blue"), blue);
    }

    private static void saveTeam(ConfigurationSection section, TeamState state) {
        section.set("active", List.copyOf(state.active));
        section.set("completed", List.copyOf(state.completed));
        section.set("seen", new ArrayList<>(state.seen));
        section.set("generated", state.generated);
        section.set("rolls", state.rolls);
    }

    public static TaskSession load(ConfigurationSection section) {
        return load(section, false);
    }

    public static TaskSession load(ConfigurationSection section, boolean requirePacing) {
        if (section == null) throw new IllegalArgumentException("Missing task snapshot");
        if (section.isSet("generation-rules-version")) {
            if (!(section.get("generation-rules-version") instanceof Integer version) || version != 2
                    || section.getConfigurationSection("preset.tier-progression") == null) {
                throw new IllegalArgumentException("Missing or invalid saved tier progression rules");
            }
        }
        for (String key : List.of("preset", "copper-extras", "total", "max-rolls", "racing", "seed", "pool", "sequences.red", "sequences.blue", "red", "blue")) {
            if (!section.isSet(key)) throw new IllegalArgumentException("Missing snapshot field: " + key);
        }
        if (!section.isBoolean("copper-extras") || !section.isBoolean("racing")
                || !(section.get("total") instanceof Integer) || !(section.get("max-rolls") instanceof Integer)
                || !(section.get("seed") instanceof Integer || section.get("seed") instanceof Long)) {
            throw new IllegalArgumentException("Invalid snapshot rule types");
        }
        List<PoolEntry> savedPool = section.getStringList("pool").stream().map(PoolEntry::decode).toList();
        TaskSession session = new TaskSession(DifficultyPreset.loadSnapshot(section.getConfigurationSection("preset"), requirePacing),
                section.getBoolean("copper-extras"), section.getInt("total"), section.getInt("max-rolls"),
                section.getBoolean("racing"), section.getLong("seed"), savedPool, false);
        if (savedPool.size() != session.pool.size()) throw new IllegalArgumentException("Snapshot pool violates match rules");
        session.restoreTeam(section.getConfigurationSection("red"), session.red);
        session.restoreTeam(section.getConfigurationSection("blue"), session.blue);
        session.redSequence.addAll(section.getStringList("sequences.red"));
        session.blueSequence.addAll(section.getStringList("sequences.blue"));
        for (List<String> sequence : List.of(session.redSequence, session.blueSequence)) {
            if (sequence.size() > session.total || new HashSet<>(sequence).size() != sequence.size()
                    || !session.indexed.keySet().containsAll(sequence)) throw new IllegalArgumentException("Invalid saved Racing sequence");
        }
        if (session.redSequence.size() < session.red.generated
                || (session.racing ? session.redSequence : session.blueSequence).size() < session.blue.generated
                || (session.racing && !session.blueSequence.isEmpty())) {
            throw new IllegalArgumentException("Incomplete saved shared/team sequence");
        }
        return session;
    }

    private void restoreTeam(ConfigurationSection section, TeamState state) {
        if (section == null) throw new IllegalArgumentException("Missing team snapshot");
        for (String key : List.of("active", "completed", "seen", "generated", "rolls")) {
            if (!section.isSet(key)) throw new IllegalArgumentException("Missing team snapshot field: " + key);
        }
        state.active.addAll(section.getStringList("active"));
        state.completed.addAll(section.getStringList("completed"));
        List<String> seen = section.getStringList("seen");
        state.seen.addAll(seen);
        state.generated = section.getInt("generated");
        state.rolls = section.getInt("rolls");
        Set<String> tasks = new HashSet<>(state.completed);
        boolean distinct = tasks.size() == state.completed.size();
        for (String id : state.active) distinct &= tasks.add(id);
        if (!distinct || state.seen.size() != seen.size() || !state.seen.containsAll(tasks)
                || !indexed.keySet().containsAll(state.seen) || state.active.size() > 4
                || state.generated != state.active.size() + state.completed.size()
                || state.generated > total || state.generated < Math.min(4, total)
                || state.active.size() != Math.min(4, total - state.completed.size())
                || state.rolls < 0 || state.rolls > maxRolls
                || state.active.stream().anyMatch(id -> !preset.unlocked(indexed.get(id), state.completed.size(), total))) {
            throw new IllegalArgumentException("Invalid saved team task state");
        }
        validateCapacity(state, state.completed.size());
    }
}
