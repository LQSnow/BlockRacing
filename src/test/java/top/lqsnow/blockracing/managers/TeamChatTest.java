package top.lqsnow.blockracing.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeamChatTest {
    @Test
    void recognizesSupportedGlobalPrefixes() {
        assertEquals(1, TeamChat.globalPrefixLength("@hello"));
        assertEquals(1, TeamChat.globalPrefixLength("＠hello"));
        assertEquals(1, TeamChat.globalPrefixLength("!hello"));
        assertEquals(1, TeamChat.globalPrefixLength("！hello"));
        assertEquals(1, TeamChat.globalPrefixLength("﹗hello"));
    }

    @Test
    void leavesOrdinaryTeamMessagesUntouched() {
        assertEquals(0, TeamChat.globalPrefixLength("hello"));
        assertEquals(0, TeamChat.globalPrefixLength(""));
    }
}
