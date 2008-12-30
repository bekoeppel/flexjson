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
}
