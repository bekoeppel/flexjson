package flexjson.mock.superhero;

import java.util.List;
import java.util.Arrays;

public class Villian {

    private String name;
    private Hero nemesis;
    private SecretLair lair;
    private List<SuperPower> powers;

    protected Villian() {
    }

    public Villian(String name, Hero nemesis, SecretLair lair, SuperPower... powers ) {
        this.name = name;
        this.nemesis = nemesis;
        this.lair = lair;
        this.powers = Arrays.asList( powers );
    }

    public String getName() {
        return name;
    }

    public Hero getNemesis() {
        return nemesis;
    }

    public SecretLair getLair() {
        return lair;
    }

    public List<SuperPower> getPowers() {
        return powers;
    }
}
