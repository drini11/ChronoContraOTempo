package br.mackenzie.chrono.logic;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class RunSession {
    public final String id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    
    private final int[] attempts = new int[Phase.count()];
    private final List<PhaseStats> cleared = new ArrayList<>();

    public int nextAttempt(int phaseIndex) {
        return ++attempts[phaseIndex];
    }

    public void addCleared(PhaseStats stats) {
        cleared.add(stats);
    }

    public List<PhaseStats> cleared() {
        return cleared;
    }

    public int totalScore() {
        int sum = 0;
        for (PhaseStats s : cleared) sum += s.score;
        return sum;
    }
}
