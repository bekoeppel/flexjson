package flexjson.test.mock.superhero;

public class Flight implements SuperPower {

    private float velocity;

    protected Flight() {
    }

    public Flight(float velocity) {
        this.velocity = velocity;
    }

    public float getVelocity() {
        return velocity;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Flight flight = (Flight) o;

        if (Float.compare(flight.velocity, velocity) != 0) return false;

        return true;
    }

    public int hashCode() {
        return (velocity != +0.0f ? Float.floatToIntBits(velocity) : 0);
    }
}
