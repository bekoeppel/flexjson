package flexjson.test.mock.superhero;

import java.util.List;
import java.util.Arrays;

public class Hero {

    private SecretLair lair;
    private SecretIdentity identity;
    private String name;
    private List<SuperPower> powers;

    protected Hero() {
    }

    public Hero(String name, SecretIdentity identity, SecretLair lair, SuperPower... powers) {
        this.name = name;
        this.identity = identity;
        this.lair = lair;
        this.powers = Arrays.asList( powers );
    }

    public SecretLair getLair() {
        return lair;
    }

    public SecretIdentity getIdentity() {
        return identity;
    }

    public String getName() {
        return name;
    }

    public List<SuperPower> getPowers() {
        return powers;
    }
}
