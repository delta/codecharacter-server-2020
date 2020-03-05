package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.request.Simulation.SimulateMatchRequest;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.enums.Division;
import delta.codecharacter.server.util.enums.MatchMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class AutoMatchUpService {

    private final Logger LOG = Logger.getLogger(AutoMatchUpService.class.getName());

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private ConstantService constantService;

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Calculate number of seconds since midnight on GMT+5:30
     *
     * @return Number of seconds
     */
    private Long getSecondsSinceMidnight() {
        Date date = new Date();
        ZonedDateTime now = date.toInstant().atZone(ZoneId.of("+05:30"));
        Instant midnight = now.toLocalDate().atStartOfDay(now.getZone()).toInstant();
        Duration duration = Duration.between(midnight, Instant.now());
        return duration.getSeconds();
    }

    /**
     * Execute matches between all DIV 1 players
     */
    private void executeMatchUp() {
        var div1Players = leaderboardService.getLeaderboardDataByDivision(Division.DIV_1);
        var playerCount = div1Players.size();

        for (var player1Index = 0; player1Index < playerCount; player1Index++) {
            for (var player2Index = player1Index + 1; player2Index < playerCount; player2Index++) {
                var player1UserId = div1Players.get(player1Index).getUserId();
                var player2UserId = div1Players.get(player2Index).getUserId();
                LOG.info("Match between " + player1UserId + " - " + player2UserId);

                SimulateMatchRequest simulateMatchRequest = SimulateMatchRequest.builder()
                        .playerId1(player1UserId.toString())
                        .playerId2(player2UserId.toString())
                        .matchMode(MatchMode.AUTO.toString())
                        .build();
                simulationService.simulateMatch(simulateMatchRequest);
            }
        }
    }

    /**
     * Get number of seconds before next scheduled match up
     * NOTE: Match ups are executed every frequency seconds since midnight (including)
     *
     * @param frequency Frequency between two match ups
     * @return Wait time in seconds
     */
    private Long getWaitTimeSeconds(Long frequency) {
        var secondsSinceMidnight = getSecondsSinceMidnight();
        var nextMatchUpTime = (((secondsSinceMidnight + frequency - 1) / frequency) * frequency);
        return (nextMatchUpTime - secondsSinceMidnight);
    }

    public void init(ContextRefreshedEvent ce) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        LOG.info("Starting auto match-up service...");

        // TODO: Fetch from constants
        // Match up is executed every frequency seconds from midnight
        var autoMatchUpFrequencySeconds = Long.parseLong(constantService.getConstantValueByKey("AUTO_MATCH_UP_FREQUENCY_SECONDS"));

        // Wait to synchronize the scheduler with match frequency
        try {
            var waitTime = getWaitTimeSeconds(autoMatchUpFrequencySeconds);
            LOG.info(waitTime.toString());
            TimeUnit.SECONDS.sleep(waitTime);
        } catch (InterruptedException e) {
            LOG.severe(e.toString());
        }

        scheduler.scheduleAtFixedRate(() -> {
            LOG.info("Matching Up Players...");
            this.executeMatchUp();
        }, 0, autoMatchUpFrequencySeconds, TimeUnit.SECONDS);
    }
}
