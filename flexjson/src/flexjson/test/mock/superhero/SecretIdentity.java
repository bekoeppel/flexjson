package flexjson.test.mock.superhero;

public class SecretIdentity {

    private String name;

    protected SecretIdentity() {
    }

    public SecretIdentity( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
