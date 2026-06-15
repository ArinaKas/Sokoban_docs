package score;

import java.io.*;
import java.util.*;

public class ScoreManager {
    private static final String SCORES_FILE = "scores.dat";
    private List<ScoreRecord> records;
    
    public ScoreManager() {
        records = new ArrayList<>();
        loadScores();
    }
    
    public void addScore(long timeSeconds, int moves, int levelDifficulty) {
        ScoreRecord record = new ScoreRecord(timeSeconds, moves, levelDifficulty);
        records.add(record);
        Collections.sort(records);
        saveScores();
    }
    
    public List<ScoreRecord> getTopScores(int limit) {
        return records.subList(0, Math.min(limit, records.size()));
    }
    
    public List<ScoreRecord> getAllScores() {
        return new ArrayList<>(records);
    }
    
    private void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORES_FILE))) {
            oos.writeObject(records);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении рекордов: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadScores() {
        File file = new File(SCORES_FILE);
        if (!file.exists()) {
            records = new ArrayList<>();
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORES_FILE))) {
            records = (List<ScoreRecord>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при загрузке рекордов: " + e.getMessage());
            records = new ArrayList<>();
        }
    }
    public void clearScores() {
        if (records != null) {
            records.clear(); // Очищаем список в памяти
            saveScores();    // Перезаписываем файл пустым списком
        }
    }
}
