package party.dargy.gtbsolver.hud;

import cc.polyfrost.oneconfig.hud.TextHud;
import cc.polyfrost.oneconfig.libs.universal.UChat;
import net.minecraft.client.Minecraft;
import party.dargy.gtbsolver.config.GTBConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BuildBattleHud extends TextHud {
    
    private boolean isWaiting = false;
    private List<String> wordList = new ArrayList<>();
    private String currentClue = "";
    private List<String> possibleWords = new ArrayList<>(); // words to show up in HUD
    private Timer autoSendTimer;
    private Random random = new Random();

    public Map<String, Map<String, Integer>> blockWeights = new HashMap<>();
    private Set<String> currentGameBlocks = new HashSet<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private File jsonFile;
    private String lastRevealedWord = "";

    public BuildBattleHud() {
        super(false);
        loadWordList();
        initializeBlockWeights();
    }
    
    private void loadWordList() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("wordlist.txt");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        wordList.add(line.toLowerCase());
                    }
                }
                reader.close();
                System.out.println("Loaded " + wordList.size() + " words from wordlist.txt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void getLines(List<String> lines, boolean example) {
        if (example) {
            lines.add("§8__§ab§8_§ae§8_");
            lines.add("§7Possible words:");
            lines.add("§fCobweb");
            lines.add("§fGoblet");
            lines.add("§fRubber");
            lines.add("§fTablet");
            return;
        }
        
        if (isWaiting) {
            if (possibleWords.isEmpty()) {
                lines.add("§eWaiting...");
            } else if (!currentClue.contains("_")) {
                lines.add("§aCompleted: §7" + currentClue);
            } else {
                lines.add(formatClue(currentClue));
                lines.add("§7Possible words:");
                for (int i = 0; i < Math.min(possibleWords.size(), 10); i++) {
                    lines.add("§f" + possibleWords.get(i));
                }
                if (possibleWords.size() > 10) {
                    lines.add("§7...and " + (possibleWords.size() - 10) + " more");
                }
            }
        } else {
            lines.add("§7Idle");
        }
    }

    private static String formatClue(String clue) {
        StringBuilder formatted = new StringBuilder();
        boolean inLetters = false;
        boolean inUnderscores = false;
        
        for (char c : clue.toCharArray()) {
            if (Character.isLetter(c)) {
                if (!inLetters) {
                    formatted.append("§a");
                    inLetters = true;
                    inUnderscores = false;
                }
                formatted.append(c);
            } else {
                if (!inUnderscores) {
                    formatted.append("§8");
                    inUnderscores = true;
                    inLetters = false;
                }
                formatted.append(c);
            }
        }
        
        return formatted.toString();
    }
    
    public void onChatMessage(String message) {
        if (!GTBConfig.buildBattleHelper) {
            return;
        }
        
        String cleanMessage = message.replaceAll("§[0-9a-fk-or]", "");

        if (cleanMessage.contains("The theme was: ")) {
            String revealedWord = extractRevealedWord(cleanMessage);
            if (!revealedWord.isEmpty() && !currentGameBlocks.isEmpty()) {
                recordBlockWeights(revealedWord);
                lastRevealedWord = revealedWord;
            }
        }

        if (cleanMessage.startsWith("Builder: ") || cleanMessage.startsWith("Welcome back!")) {
            isWaiting = true;
            currentClue = "";
            possibleWords.clear();
            currentGameBlocks.clear(); // Reset blocks for new game
            if (autoSendTimer != null) {
                autoSendTimer.cancel();
                autoSendTimer = null;
            }
        }
        
        if (message.contains("Want to play again")) {
            isWaiting = false;
            currentClue = "";
            possibleWords.clear();
            currentGameBlocks.clear();
            if (autoSendTimer != null) {
                autoSendTimer.cancel();
                autoSendTimer = null;
            }
        }
    }
    
    public void onActionBarMessage(String actionBarText) {
        if (!GTBConfig.buildBattleHelper || !isWaiting) {
            return;
        }
        
        String cleanText = actionBarText.replaceAll("§[0-9a-fk-or]", "");
        
        if (cleanText.contains("theme is")) {
            int startPos = cleanText.indexOf("theme is") + "theme is".length();
            String clue = cleanText.substring(startPos).trim();
                
            if (!clue.isEmpty() && !clue.equals(currentClue)) {
                currentClue = clue;
                updatePossibleWords(clue);
            }
        }
    }

    public void onBlockPlaced(String blockName) {
        if (!GTBConfig.buildBattleHelper || !isWaiting) {
            return;
        }

        if (blockName != null && !blockName.isEmpty() && !currentGameBlocks.contains(blockName.toLowerCase())) {
            currentGameBlocks.add(blockName.toLowerCase());
            if (GTBConfig.debugMode) UChat.chat("§7[Debug] Added block: " + blockName.toLowerCase());
            
            if (currentClue.isEmpty() || !currentClue.contains("_")) {
                updatePossibleWordsByBlocks();
            }
        }
    }
    
    private void updatePossibleWords(String clue) {
        if (!clue.contains("_")) {
            return;
        }
        
        if (GTBConfig.debugMode) UChat.chat("§7[Debug] Looking for pattern: " + clue);
        possibleWords.clear();
        
        for (String word : wordList) {
            if (matchesPattern(word, clue)) {
                possibleWords.add(word);
            }
        }
        
        possibleWords.sort((a, b) -> {
            if (!currentGameBlocks.isEmpty()) {
                double scoreA = calculateBlockScore(a);
                double scoreB = calculateBlockScore(b);
                int scoreCompare = Double.compare(scoreB, scoreA); // Higher score first
                if (scoreCompare != 0) return scoreCompare;
            }
            
            int lengthCompare = Integer.compare(a.length(), b.length());
            if (lengthCompare != 0) return lengthCompare;
            
            return a.compareToIgnoreCase(b);
        });
        
        if (GTBConfig.debugMode) UChat.chat("§7[Debug] Found " + possibleWords.size() + " matches");
        if (!possibleWords.isEmpty()) {
            UChat.chat("§7All possible words: §f" + String.join(", ", possibleWords));
        }
        
        if (GTBConfig.autoSendBestGuess && possibleWords.size() == 1) {
            scheduleAutoSend(possibleWords.get(0));
        } else if (autoSendTimer != null) {
            autoSendTimer.cancel();
            autoSendTimer = null;
        }
    }
    
    private void scheduleAutoSend(String word) {
        if (autoSendTimer != null) {
            autoSendTimer.cancel();
        }

        long delay = (long) GTBConfig.autoSendDelay;
        
        if (GTBConfig.debugMode) {
            UChat.chat("§7[Debug] Auto-sending '" + word + "' in " + delay + "ms");
        }
        
        autoSendTimer = new Timer();
        autoSendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                UChat.say(word);
                if (GTBConfig.debugMode) {
                    UChat.chat("§7[Debug] Auto-sent: " + word);
                }
            }
        }, delay);
    }
        
    private boolean matchesPattern(String word, String pattern) {
        String normalizedWord = word.toLowerCase();
        String normalizedPattern = pattern.toLowerCase();
        
        if (normalizedWord.length() != normalizedPattern.length()) {
            return false;
        }
        
        for (int i = 0; i < normalizedWord.length(); i++) {
            char wordChar = normalizedWord.charAt(i);
            char patternChar = normalizedPattern.charAt(i);
            
            if (Character.isLetter(patternChar)) {
                if (wordChar != patternChar) {
                    return false;
                }
            }
            
            else if (patternChar == '_') {
                if (!Character.isLetter(wordChar)) {
                    return false;
                }
            }

            else {
                if (wordChar != patternChar) {
                    return false;
                }
            }
        }
        
        return true;
    }

    public void initializeBlockWeights() {
        try {
            File gameDir = Minecraft.getMinecraft().mcDataDir;
            File configFolder = new File(gameDir, "config");
            if (!configFolder.exists()) configFolder.mkdirs();
            jsonFile = new File(configFolder, "gtb.json");

            loadBlockWeights(jsonFile);

            if (GTBConfig.debugMode) UChat.chat("§7[Debug] Block weights system initialized at: " + jsonFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            UChat.chat("§c[GTBSolver] Failed to initialize block weights system: " + e.getMessage());
        }
    }

    private void loadBlockWeights(File file) {
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, Map<String, Integer>>>(){}.getType();
                Map<String, Map<String, Integer>> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    blockWeights = loaded;
                    if (GTBConfig.debugMode) UChat.chat("§7[Debug] Loaded block weights for " + blockWeights.size() + " words");
                }
            } catch (Exception e) {
                e.printStackTrace();
                UChat.chat("§c[GTBSolver] Failed to load block weights: " + e.getMessage());
            }
        } else {
            if (GTBConfig.debugMode) UChat.chat("§7[Debug] No existing block weights file found, starting fresh");
        }
    }
    
    private void saveBlockWeights() {
        try {
            String json = GSON.toJson(blockWeights);
            Files.write(jsonFile.toPath(), json.getBytes());
            if (GTBConfig.debugMode) UChat.chat("§7[Debug] Saved block weights for " + blockWeights.size() + " words");
        } catch (Exception e) {
            e.printStackTrace();
            UChat.chat("§c[GTBSolver] Failed to save block weights: " + e.getMessage());
        }
    }
    
    public void reset() {
        isWaiting = false;
        currentClue = "";
        possibleWords.clear();
        currentGameBlocks.clear();
        
        if (autoSendTimer != null) {
            autoSendTimer.cancel();
            autoSendTimer = null;
        }
    }

    private String extractRevealedWord(String message) {
        try {
            int index = message.indexOf("The theme was:");
            if (index != -1) {
                String remaining = message.substring(index + "The theme was:".length()).trim();

                String[] words = remaining.split("[^a-zA-Z]+");
                if (words.length > 0 && !words[0].isEmpty()) {
                    return words[0].toLowerCase();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    private void recordBlockWeights(String word) {
        try {
            if (!blockWeights.containsKey(word)) {
                blockWeights.put(word, new HashMap<>());
            }
            
            Map<String, Integer> wordBlocks = blockWeights.get(word);
            
            // Add 1 point for each unique block type used
            for (String block : currentGameBlocks) {
                wordBlocks.put(block, wordBlocks.getOrDefault(block, 0) + 1);
            }
            
            saveBlockWeights();
            
            if (GTBConfig.debugMode) {
                UChat.chat("§7[Debug] Recorded " + currentGameBlocks.size() + " block types for word: " + word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updatePossibleWordsByBlocks() {
        if (currentGameBlocks.isEmpty()) {
            return;
        }
        
        Map<String, Double> wordScores = new HashMap<>();
        
        for (String word : wordList) {
            double score = calculateBlockScore(word);
            if (score > 0) {
                wordScores.put(word, score);
            }
        }
        
        possibleWords.clear();
        wordScores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(20) // Limit to top 20 to avoid spam
            .forEach(entry -> possibleWords.add(entry.getKey()));
        
        if (GTBConfig.debugMode && !possibleWords.isEmpty()) {
            UChat.chat("§7[Debug] Updated word predictions based on " + currentGameBlocks.size() + " block types");
        }
    }
    
    private double calculateBlockScore(String word) {
        if (!blockWeights.containsKey(word)) {
            return 0.0;
        }
        
        Map<String, Integer> wordBlocks = blockWeights.get(word);
        double score = 0.0;
        
        for (String placedBlock : currentGameBlocks) {
            if (wordBlocks.containsKey(placedBlock)) {
                score += wordBlocks.get(placedBlock);
            }
        }
        
        return score;
    }
}
