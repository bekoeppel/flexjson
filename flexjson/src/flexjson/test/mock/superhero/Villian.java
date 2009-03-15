package flexjson.test.mock.superhero;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class Villian {

    private String name;
    private Hero nemesis;
    private SecretLair lair;
    private List<SuperPower> powers;

    protected Villian() {
        powers = new ArrayList<SuperPower>();
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Villian villian = (Villian) o;

        if (lair != null ? !lair.equals(villian.lair) : villian.lair != null) return false;
        if (name != null ? !name.equals(villian.name) : villian.name != null) return false;
        if (nemesis != null ? !nemesis.equals(villian.nemesis) : villian.nemesis != null) return false;
        if (powers != null ? !powers.equals(villian.powers) : villian.powers != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (nemesis != null ? nemesis.hashCode() : 0);
        result = 31 * result + (lair != null ? lair.hashCode() : 0);
        result = 31 * result + (powers != null ? powers.hashCode() : 0);
        return result;
    }
}
