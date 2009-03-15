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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hero hero = (Hero) o;

        if (identity != null ? !identity.equals(hero.identity) : hero.identity != null) return false;
        if (lair != null ? !lair.equals(hero.lair) : hero.lair != null) return false;
        if (name != null ? !name.equals(hero.name) : hero.name != null) return false;
        if (powers != null ? !powers.equals(hero.powers) : hero.powers != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (lair != null ? lair.hashCode() : 0);
        result = 31 * result + (identity != null ? identity.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (powers != null ? powers.hashCode() : 0);
        return result;
    }
}
