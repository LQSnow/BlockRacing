package top.lqsnow.blockracing.commands;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomTeamTest {
    @Test
    void splitsAllPlayersIntoBalancedTeams() {
        List<Integer> players = List.of(1, 2, 3, 4, 5, 6, 7);

        List<List<Integer>> teams = RandomTeam.splitPlayers(players, new Random(42));

        assertEquals(players.size(), teams.get(0).size() + teams.get(1).size());
        assertTrue(Math.abs(teams.get(0).size() - teams.get(1).size()) <= 1);
        assertEquals(new HashSet<>(players),
                new HashSet<>(teams.stream().flatMap(List::stream).toList()));
    }

    @Test
    void keepsEvenTeamsEqual() {
        List<List<Integer>> teams = RandomTeam.splitPlayers(
                List.of(1, 2, 3, 4, 5, 6),
                new Random(7)
        );

        assertEquals(teams.get(0).size(), teams.get(1).size());
    }
}
