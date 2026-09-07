package com.golcmit.alodestar.portal;

public class SpatialTransform {

    private static final double L = 4096.0;
    private static final double SCALE = 1.0 / 8.0;
    private static final long PHI = 0x9E3779B9L; // 黄金比に基づく定数（よく使われる）
    private static final long P1 = 73856093L;   // 適当な大きな素数1
    private static final long P2 = 19349663L;   // 適当な大きな素数2
    private static final long P3 = 83492791L;   // 適当な大きな素数3
    // 座標を保持するためのレコード
    public record Point(double x, double z) {}

    /**
     * 変換関数
     * @param n セルの番号(x座標)
     * @param m セルの番号(z座標)
     * @param  seed シード値
     * @return h 適当な数
     */

    private static long randomizer (long n,long m,long seed){

        long h = (n * P1) ^ (m * P2) ^ (seed * P3); // 各要素に素数を掛けて XOR で混ぜる
        h ^= (h >>> 16);                            // 上位ビットを回してさらに混ぜる
        h *= PHI;                                   // 仕上げに大きな数を掛ける
        h ^= (h >>> 13);

        return  h;
    }
    /**
     * 変換関数
     * @param x 元のx座標
     * @param z 元のz座標
     * @param theta 全体の座標系の回転角 (ラジアン)
     * @param  seed シード値
     * @return 変換後の座標 (s, t)
     */
    public static Point forward(double x, double z, double theta,long seed) {
        // 1. 全体の座標系を theta 回転させる
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        double X = x * cosTheta - z * sinTheta;
        double Z = x * sinTheta + z * cosTheta;

        // 2. セルのインデックス (n, m) と相対位置 (X_prime, Z_prime) を算出
        // Math.floor を用いて負の領域でも正しく切り捨てる
        long n = (long) Math.floor(X / L + 0.5);
        long m = (long) Math.floor(Z / L + 0.5);
        double X_prime = X - n * L;
        double Z_prime = Z - m * L;

        // 3. セル内部の回転 (phi = (n + m) mod 4 * (pi/2))
        // Javaの '%' は負の数を正しく処理できないため Math.floorMod を使用
        int k = Math.floorMod(randomizer(n,m,seed), 4);
        double X_double_prime = 0;
        double Z_double_prime = 0;

        // 誤差を防ぐため、三角関数ではなく成分の直接操作で 90度単位の回転を実現
        switch (k) {
            case 0 -> { X_double_prime = X_prime;  Z_double_prime = Z_prime; }
            case 1 -> { X_double_prime = -Z_prime; Z_double_prime = X_prime; } // 90度回転
            case 2 -> { X_double_prime = -X_prime; Z_double_prime = -Z_prime; } // 180度回転
            case 3 -> { X_double_prime = Z_prime;  Z_double_prime = -X_prime; } // 270度回転
        }

        // 4. 縮小と再配置
        double s = SCALE * (n * L + X_double_prime);
        double t = SCALE * (m * L + Z_double_prime);

        return new Point(s, t);
    }

    /**
     * 逆変換関数
     * @param s 変換後のs座標
     * @param t 変換後のt座標
     * @param theta 全体の座標系の回転角 (ラジアン)
     * @param seed シード値
     * @return 元の座標 (x, z)
     */
    public static Point inverse(double s, double t, double theta,long seed) {
        double cellWidth = L * SCALE; // 変換後のセルの幅 (4096 / 8 = 512)

        // 1. 変換後の座標から、属しているセルのインデックスを特定
        long n = (long) Math.floor(s / cellWidth + 0.5);
        long m = (long) Math.floor(t / cellWidth + 0.5);

        // 2. セル内部の相対位置を抽出し、元のサイズに拡大
        double s_prime = s - n * cellWidth;
        double t_prime = t - m * cellWidth;
        double X_double_prime = s_prime / SCALE;
        double Z_double_prime = t_prime / SCALE;

        // 3. セル内部の逆回転 (-phi)
        int k = Math.floorMod(randomizer(n,m,seed), 4);
        double X_prime = 0;
        double Z_prime = 0;

        // forward関数とは逆方向へ成分を回転させる
        switch (k) {
            case 0 -> { X_prime = X_double_prime;  Z_prime = Z_double_prime; }
            case 1 -> { X_prime = Z_double_prime;  Z_prime = -X_double_prime; } // -90度回転
            case 2 -> { X_prime = -X_double_prime; Z_prime = -Z_double_prime; } // -180度回転
            case 3 -> { X_prime = -Z_double_prime; Z_prime = X_double_prime; }  // -270度回転
        }

        // 4. 傾いた座標系での全体位置を復元
        double X = n * L + X_prime;
        double Z = m * L + Z_prime;

        // 5. 座標系全体の逆回転 (-theta)
        // 回転角の符号を反転させて元の (x, z) 系へ戻す
        double cosTheta = Math.cos(-theta);
        double sinTheta = Math.sin(-theta);
        double x = X * cosTheta - Z * sinTheta;
        double z = X * sinTheta + Z * cosTheta;

        return new Point(x, z);
    }


}