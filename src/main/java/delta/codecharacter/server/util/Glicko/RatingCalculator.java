package delta.codecharacter.server.util.Glicko;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Based on http://www.glicko.net/glicko/glicko.pdf
public class RatingCalculator {

    // Constants
    private Double q = (Math.log(10) / 400);

    private Double minRatingDeviation = 50d;

    // Amount which decides how much a player's rating deviation changes per 5 mins
    private Double c = 416d;

    // Time for one rating period in minutes
    private Double timePeriod = 5d;

    /**
     * Calculate g(rd)
     *
     * @param rd Rating Deviation
     * @return g(rd)
     */
    private Double calculateGRD(Double rd) {
        Double numerator = 1d + 3d * (q * q) * (rd * rd);
        Double pi = Math.PI;
        Double denominator = pi * pi;
        return (1d / Math.sqrt(numerator / denominator));
    }

    /**
     * Calculate expected outcome of the match
     *
     * @param r1  Rating of player 1
     * @param r2  Rating of player 2
     * @param rd2 Rating Deviation of player 2
     * @return E(s | r1, r2, rd2)
     */
    private Double calculateExpectation(Double r1, Double r2, Double rd2) {
        Double grd = calculateGRD(rd2);
        double exponent = -grd * (r1 - r2) / 400d;
        double denominator = (1 + Math.pow(10, exponent));
        return (1d / denominator);
    }

    /**
     * Calculate value of d^2
     *
     * @return
     */
    private Double calculateD2(Double rating, List<GlickoRating> opponentRatings) {
        double sum = 0d;
        for (var opponentRating : opponentRatings) {
            Double grd = calculateGRD(opponentRating.getRatingDeviation());
            double expectation = calculateExpectation(rating, opponentRating.getRating(), opponentRating.getRatingDeviation());
            sum += (grd*grd)*expectation*(1 - expectation);
        }

        return (1d / (q *q * sum));
    }

    /**
     * Based on player's ratings and opponent's ratings, new rating deviation is calculated
     * at the end of current period
     *
     * @param playerRating Glicko Rating of player
     * @param opponentRatings Glicko Ratings of all players who matched against player
     * @return New Rating deviation
     */
    public Double calculateNewRatingDeviation(GlickoRating playerRating, List<GlickoRating> opponentRatings) {
        Double d2 = calculateD2(playerRating.getRating(), opponentRatings);
        Double rd = playerRating.getRatingDeviation();
        double value = (1d / (rd * rd)) + (1d / d2);

        return Math.min(Math.sqrt(1d / value), minRatingDeviation);
    }

    /**
     * Get the new rating of player at the end of period
     * @param playerRating player's glicko rating
     * @param opponentRatings glicko ratings of all opponents
     * @param opponentOutcomes Outcomes with each opponent (1 - Player wins, 0 - Player loses, 0.5 - Tie)
     * @return New Rating
     */
    public Double calculateNewRating(GlickoRating playerRating, List<GlickoRating> opponentRatings, List<Double> opponentOutcomes) {
        Double d2 = calculateD2(playerRating.getRating(), opponentRatings);
        Double rd = playerRating.getRatingDeviation();
        Double r = playerRating.getRating();

        double sum = 0d;
        int numOpponents = opponentRatings.size();

        for (int i = 0; i < numOpponents; i++) {
            Double grd = calculateGRD(opponentRatings.get(i).getRatingDeviation());
            Double s = opponentOutcomes.get(i);
            Double expectation = calculateExpectation(r, opponentRatings.get(i).getRating(), opponentRatings.get(i).getRatingDeviation());
            sum += grd * (s - expectation);
        }

        return r + (q * sum) / ((1 / (rd*rd)) + (1 / d2));
    }

    /**
     * The rating deviation to be computed just before calculating match ratings
     * @param rd Rating deviation of player
     * @return Current latest rating deviation after taking time into consideration
     */
    public Double getCurrentRatingDeviation(Double rd, Date lastMatchTime) {
        Date currentDate = new Date();
        long duration = currentDate.getTime() - lastMatchTime.getTime();

        Double numTimePeriods = TimeUnit.MILLISECONDS.toMinutes(duration) / timePeriod;
        return Math.min(Math.sqrt(rd * rd + c * numTimePeriods), 350d);
    }
}
