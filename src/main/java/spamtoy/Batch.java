package spamtoy;

import com.weblyzard.lib.string.nilsimsa.Nilsimsa;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Batch {
    Nilsimsa[] hashes;
    int[][] scores;
    double[] prs;

    public Batch(String[] texts) {
        hashes = Arrays.stream(texts).map(Nilsimsa::getHash).toArray(Nilsimsa[]::new);

        // calculate scores of each item in comparison to other items
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

        prs = new double[scores.length];
        for (int i = 0; i < scores.length; i++)
            prs[i] = prob(i);
    }

    /**
     * calculate the average score of item at index i, using:
     *      sum_of_scores_of_i = sum_by_j(scores[i][j]) + sum_by_k(scores[k][i]) with k > i
     * then use the percentage between the average score and 256 as the spam probability.
     */
    private double prob(int i) {
        double score_sum = 0;
        if (i > 0)
            score_sum += IntStream.of(scores[i]).asDoubleStream().sum();
        for (int k = i + 1; k < scores.length; k++)
            score_sum += scores[k][i];
        double avg = score_sum / (scores.length - 1);
        return avg / 256;
    }
}
