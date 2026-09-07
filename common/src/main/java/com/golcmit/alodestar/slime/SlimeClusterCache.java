package com.golcmit.alodestar.slime;
import com.golcmit.alodestar.slime.SlimeClusterBake;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;

public final class SlimeClusterCache {
    // 256クラスタ（16384×16384チャンク分）を保持してもメモリ消費はわずか130KB程度
    private static final int MAX_CLUSTERS = 256;
    private static final Long2ObjectLinkedOpenHashMap<long[]> CACHE = new Long2ObjectLinkedOpenHashMap<>(MAX_CLUSTERS);

    private SlimeClusterCache() {}

    public static boolean isSlimeChunk(int chunkX, int chunkZ, long worldSeed) {
        int cx = chunkX >> 6;
        int cz = chunkZ >> 6;
        int lx = chunkX & 63;
        int lz = chunkZ & 63;
        long key = (((long) cx) << 32) | (cz & 0xFFFFFFFFL);
        long[] bitmap;

        synchronized (CACHE) {
            bitmap = CACHE.get(key);
            if (bitmap == null) {
                if (CACHE.size() >= MAX_CLUSTERS) {
                    CACHE.removeFirst(); // 最も古いクラスタを破棄
                }
                bitmap = SlimeClusterBake.bakeCluster(cx, cz, worldSeed);
                CACHE.put(key, bitmap);
            }
        }

        return (bitmap[lz] & (1L << lx)) != 0;
    }
}