package org.vaadin.example;

import com.helger.commons.csv.CSVReader;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Martin Vysny <mavi@vaadin.com>
 */
public class Player {
    private String name;
    private String team;
    private String position;
    private int heightInches;
    private int weightLbs;
    private float age;

    public Player() {
    }

    public Player(String name, String team, String position, int heightInches, int weightLbs, float age) {
        this.name = name;
        this.team = team;
        this.position = position;
        this.heightInches = heightInches;
        this.weightLbs = weightLbs;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getHeightInches() {
        return heightInches;
    }

    public void setHeightInches(int heightInches) {
        this.heightInches = heightInches;
    }

    public int getWeightLbs() {
        return weightLbs;
    }

    public void setWeightLbs(int weightLbs) {
        this.weightLbs = weightLbs;
    }

    public float getAge() {
        return age;
    }

    public void setAge(float age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", team='" + team + '\'' +
                ", position='" + position + '\'' +
                ", heightInches=" + heightInches +
                ", weightLbs=" + weightLbs +
                ", age=" + age +
                '}';
    }

    @NotNull
    public static List<Player> loadFromCSV() throws IOException {
        final List<Player> list = new ArrayList<Player>();
        try (InputStream is = Player.class.getClassLoader().getResourceAsStream("mlb_players.csv")) {
            final CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
            reader.setSkipLines(1);
            reader.forEach(row -> {
                if (!row.isEmpty()) {
                    try {
                        final Player player = new Player(row.get(0).trim(),
                                row.get(1).trim(),
                                row.get(2).trim(),
                                Integer.parseInt(row.get(3).trim()),
                                Integer.parseInt(row.get(4).trim()),
                                Float.parseFloat(row.get(5).trim())
                        );
                        list.add(player);
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to parse row " + row, ex);
                    }
                }
            });
        }
        return list;
    }

    @NotNull
    public static final List<Player> ALL_PLAYERS;
    static {
        try {
            ALL_PLAYERS = Collections.unmodifiableList(loadFromCSV());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
