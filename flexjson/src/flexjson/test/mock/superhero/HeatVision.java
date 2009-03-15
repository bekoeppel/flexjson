package flexjson.test.mock.superhero;

public class HeatVision implements SuperPower {
    private float power;

    protected HeatVision() {
    }

    public HeatVision(float power) {
        this.power = power;
    }

    public float getPower() {
        return power;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HeatVision that = (HeatVision) o;

        if (Float.compare(that.power, power) != 0) return false;

        return true;
    }

    public int hashCode() {
        return (power != +0.0f ? Float.floatToIntBits(power) : 0);
    }
}
