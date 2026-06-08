package com.bk.arenax.domain.matches;

public enum MatchFormat {
    ONE_VS_ONE(2),
    TWO_VS_TWO(4),
    CUSTOM(-1);

    private final int requiredPlayers;

    MatchFormat(int requiredPlayers) {
        this.requiredPlayers = requiredPlayers;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }
}