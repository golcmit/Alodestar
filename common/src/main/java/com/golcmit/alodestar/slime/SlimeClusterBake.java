package com.golcmit.alodestar.slime;



public final class SlimeClusterBake {
    private static final long[] LCG_A_TABLE = {
            1664525L, 1103515245L, 214013L, 22695477L,
            134775813L, 69069L, 75L, 65539L
    };
    private static final long MASK_32 = 0xFFFFFFFFL;
    private static final int THRESHOLD = 0x1999999A; // 上位10%をスライム判定にする閾値

    private SlimeClusterBake() {}

    /**
     * クラスタ全域（64x64チャンク）のスライム判定を512バイトのビットマップに焼き付ける
     */
    public static long[] bakeCluster(int cx, int cz, long worldSeed) {
        long mixed = worldSeed ^ hash64(((long) cx * 0x2545F4914F6CDD1DL) + cz);
        long grngState = hash64(mixed);

        int pick = (int) Math.floorMod(nextSplitMix64(grngState), 3);
        grngState += 0x9E3779B97F4A7C15L;

        long p1 = nextSplitMix64(grngState); grngState += 0x9E3779B97F4A7C15L;
        long p2 = nextSplitMix64(grngState); grngState += 0x9E3779B97F4A7C15L;
        long p3 = nextSplitMix64(grngState); grngState += 0x9E3779B97F4A7C15L;
        int  p4 = (int) nextSplitMix64(grngState);

        long[] bitmap = new long[64]; // 64行 x 64bit

        switch (pick) {
            case 0 -> bakeLcg(bitmap, p1, p2, p3);
            case 1 -> bakeAdditive(bitmap, p1, p4);
            case 2 -> bakeMsws(bitmap, p1, p2, p4);
        }

        return bitmap;
    }

    // --- 各BRNGのベイク処理（インラインでビットマップを直接詰める） ---

    private static void bakeLcg(long[] bitmap, long b, long x0, long settings) {
        long a = LCG_A_TABLE[(int) ((settings >>> 8) & 0x07)];
        long x = x0 & MASK_32;
        b = b & MASK_32;

        int warmUp = (int) (settings & 0xFF);
        for (int i = 0; i < warmUp; i++) {
            x = (a * x + b) & MASK_32;
        }

        for (int i = 0; i < 4096; i++) {
            x = (a * x + b) & MASK_32;
            if ((int) x < THRESHOLD) {
                setBit(bitmap, i);
            }
        }
    }

    private static void bakeAdditive(long[] bitmap, long x0seed, int settings) {
        int rawLag = settings & 0x0F;
        int lag = (rawLag < 15) ? (2 + rawLag) : 16;
        int tap = 1 + (((settings >>> 4) & 0x0F) % (lag - 1));
        int rot = (settings >>> 8) & 0x1F;
        int opMode = (settings >>> 13) & 0x03;
        int multiplier = 1 + (((settings >>> 15) & 0x03) * 2);

        int[] buf = new int[16];
        int seed = (int) x0seed;
        for (int i = 0; i < lag; i++) {
            seed = 1103515245 * seed + 12345;
            buf[i] = seed;
        }

        int idx = 0;
        for (int i = 0; i < 4096; i++) {
            int i1 = idx + tap;
            if (i1 >= lag) i1 -= lag;

            int a = buf[idx];
            int b = buf[i1] * multiplier;
            int v = switch (opMode) {
                case 0 -> a + b;
                case 1 -> a - b;
                case 2 -> a ^ b;
                case 3 -> (a + b) ^ Integer.rotateLeft(b, 5);
                default -> 0;
            };
            v = Integer.rotateLeft(v, rot);
            buf[idx] = v;

            idx++;
            if (idx >= lag) idx = 0;

            if (v < THRESHOLD) {
                setBit(bitmap, i);
            }
        }
    }

    private static void bakeMsws(long[] bitmap, long x0, long weylConst, int settings) {
        int rot = settings & 0x3F;
        int skipPattern = (settings >>> 6) & 0x0F;
        long s = weylConst | 1L;
        long x = x0;
        long w = 0;

        for (int i = 0; i < 4096; i++) {
            if ((x & 0x0F) != skipPattern) {
                x *= x;
            }
            w += s;
            x += w;
            x = Long.rotateLeft(x, rot);

            if ((int) x < THRESHOLD) {
                setBit(bitmap, i);
            }
        }
    }

    /**
     * 1次元インデックス(0..4095)をビットマップの二次元座標(lx, lz)に投影する
     */
    private static void setBit(long[] bitmap, int index) {
        int lz = index >> 6;    // index / 64 (0..63)
        int lx = index & 63;   // index % 64 (0..63)
        bitmap[lz] |= (1L << lx);
    }

    private static long nextSplitMix64(long state) {
        long z = state + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static long hash64(long x) {
        x += 0x9E3779B97F4A7C15L;
        x = (x ^ (x >>> 30)) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 27)) * 0x94D049BB133111EBL;
        return x ^ (x >>> 31);
    }
}