package com.gymlink.model.enums;

import lombok.Getter;

@Getter
public enum VipRank {
    BRONCE(0, 1.0, "PLATA", 1500),
    PLATA(1500, 1.25, "ORO", 3500),
    ORO(3500, 1.5, "TITAN", 6000),
    TITAN(6000, 2.0, "TITAN", 6000);

    private final long minXp;
    private final double multiplier;
    private final String nextRankName;
    private final long nextRankXp;

    VipRank(long minXp, double multiplier, String nextRankName, long nextRankXp) {
        this.minXp = minXp;
        this.multiplier = multiplier;
        this.nextRankName = nextRankName;
        this.nextRankXp = nextRankXp;
    }

    public static VipRank fromXp(long xp) {
        if (xp >= TITAN.minXp) return TITAN;
        if (xp >= ORO.minXp) return ORO;
        if (xp >= PLATA.minXp) return PLATA;
        return BRONCE;
    }
}