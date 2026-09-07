package com.golcmit.alodestar.slime;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public final class DeterministicSlimeRandom implements RandomSource {
    public static final RandomSource TRUE_INSTANCE = new DeterministicSlimeRandom(true);
    public static final RandomSource FALSE_INSTANCE = new DeterministicSlimeRandom(false);

    private final boolean result;

    private DeterministicSlimeRandom(boolean result) {
        this.result = result;
    }

    @Override
    public int nextInt(int bound) {
        return this.result ? 0 : 1;
    }

    @Override
    public int nextInt() {
        return this.result ? 0 : 1;
    }

    // RandomSourceの実装要件を満たすためダミー配置
    @Override public RandomSource fork() { return this; }
    @Override public PositionalRandomFactory forkPositional() { throw new UnsupportedOperationException(); }
    @Override public void setSeed(long seed) {}
    @Override public long nextLong() { return 0L; }
    @Override public boolean nextBoolean() { return this.result; }
    @Override public float nextFloat() { return 0.0F; }
    @Override public double nextDouble() { return 0.0; }
    @Override public double nextGaussian() { return 0.0; }
}