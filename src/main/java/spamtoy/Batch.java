package spamtoy;

import com.weblyzard.lib.string.nilsimsa.Nilsimsa;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Batch {
    Nilsimsa[] hashes;
    int[][] scores;
    double[] prs;

    public Batch(String[] texts) {
        hashes = Arrays.stream(texts).map(Nilsimsa::getHash).toArray(Nilsimsa[]::new);

        // calculate scores of each item in comparison with other items
        scores = new int[hashes.length][];
        // scores[i] has length i (to save space and to halve the number of calls to `bitwiseDifference`).
        for (int i = 1; i < scores.length; i++) {
            scores[i] = new int[i];
            for (int j = 0; j < i; j++) {
                // `bitwiseDifference` returns x with 0 <= x <= 256 while `compare` returns y with y = 128 - x,
                // so we don't use `compare` but make our own score here: score = 256 - x
                scores[i][j] = 256 - hashes[i].bitwiseDifference(hashes[j]);
            }
        }

        // normalize all scores by subtracting `lowestScore` from each,
        // and later probability is calculated with `maxPossible` = 256 - `lowestScore` instead of with 256
        int lowestScore = Stream.of(scores).mapToInt(a -> a == null ? 256 : Arrays.stream(a).min().orElse(256))
                .min().orElse(0);
        for (int i = 1; i < scores.length; i++) {
            for (int j = 0; j < i; j++)
                scores[i][j] -= lowestScore;
        }

        prs = new double[scores.length];
        int maxPossible = 256 - lowestScore;
        for (int i = 0; i < scores.length; i++)
            prs[i] = pr(i, maxPossible);
    }

    /**
     * For item at index i, count number of items whose scores with it are higher than 80% of maxPossible.
     * Its spam probability is the ratio of that number over total number of items.
     */
    private double pr(int i, int maxPossible) {
        double scoreThreshold = 0.8 * maxPossible;
        int similarityCount = 0;
        if (i > 0)
            similarityCount += (int) IntStream.of(scores[i]).filter(c -> c > scoreThreshold).count();
        for (int k = i + 1; k < scores.length; k++)
            if (scores[k][i] > scoreThreshold)
                similarityCount++;
        return (double) similarityCount / (scores.length - 1);
    }
}
