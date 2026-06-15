package score;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScoreRecord implements Comparable<ScoreRecord>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String playerName;
    private final long timeSeconds;
    private final int moves;
    private final LocalDateTime completedAt;
    private final int levelDifficulty;
    
    public ScoreRecord(long timeSeconds, int moves, int levelDifficulty) {
        this("User", timeSeconds, moves, levelDifficulty);
    }
    
    public ScoreRecord(String playerName, long timeSeconds, int moves, int levelDifficulty) {
        this.playerName = playerName;
        this.timeSeconds = timeSeconds;
        this.moves = moves;
        this.levelDifficulty = levelDifficulty;
        this.completedAt = LocalDateTime.now();
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public long getTimeSeconds() {
        return timeSeconds;
    }
    
    public int getMoves() {
        return moves;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public int getLevelDifficulty() {
        return levelDifficulty;
    }
    
    public String getFormattedTime() {
        long minutes = timeSeconds / 60;
        long seconds = timeSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    public String getFormattedDate() {
        return completedAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
    
    @Override
    public int compareTo(ScoreRecord other) {
        if (this.timeSeconds != other.timeSeconds) {
            return Long.compare(this.timeSeconds, other.timeSeconds);
        }
        return Integer.compare(this.moves, other.moves);
    }
    
    @Override
    public String toString() {
        return String.format("%s | Время: %s | Ходов: %d | Дата: %s | Сложность: %d",
                playerName, getFormattedTime(), moves, getFormattedDate(), levelDifficulty);
    }
}
