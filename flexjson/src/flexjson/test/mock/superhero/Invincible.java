package flexjson.test.mock.superhero;

public class Invincible implements SuperPower {

    public int hashCode() {
        return 0;
    }

    public boolean equals(Object object) {
        return object instanceof Invincible;
    }
}
