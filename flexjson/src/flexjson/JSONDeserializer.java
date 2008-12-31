package flexjson;

import java.util.Map;
import java.util.HashMap;

/**
 * <p>
 * JSONDeserializer takes as input a json string and produces a static typed object graph from that
 * json representation.  By default it uses the class property in the json data in order to map the
 * untyped generic json data into a specific Java type.  However, you are limited to only json strings
 * with class information embedded when resolving it into a Java type.  But, for now let's just look at
 * the simplest case of class attributes in your json.  We'll look at how {@link JSONSerializer} and
 * JSONDeserializer pair together out of the box.
 * </p>
 * <p>
 * Say we have a simple object like Hero (see the superhero package under the test and mock).
 * To create a json represenation of Hero we'd do the following:
 * </p>
 *
 * <pre>
 *   Hero harveyBirdman = new Hero("Harvey Birdman", new SecretIdentity("Attorney At Law"), new SecretLair("Sebben & Sebben") );
 *   String jsonHarvey = new JSONSerialize().serialize(hero);
 * </pre>
 * <p>
 * Now to reconsitute Harvey to fight for the law we'd use JSONDeserializer like so:
 * </p>
 * <pre>
 *   Hero hero = new JSONDeserializer<Hero>().deserialize( jsonHarvey );
 * </pre>
 * <p>
 * Pretty easy when all the type information is included with the JSON data.  Now let's look at the more difficult
 * case of how we might reconstitute something missing type info.
 * </p>
 * <p>
 * Let's exclude the class attribute in our json like so:
 * </p>
 *
 * <pre>
 *   String jsonHarvey = new JSONSerialize().exclude("*.class").serialize(hero);
 * </pre>
 * <p>
 * The big trick here is to replace that type information when we instantiate the deserializer.
 * To do that we'll use the {@link flexjson.JSONDeserializer#use(String, Class)} method like so:
 * </p>
 * <pre>
 *   Hero hero = new JSONDeserializer<Hero>().use( null, Hero.class ).deserialize( jsonHarvey );
 * </pre>
 * <p>
 * Like riding a horse with no saddle without our type information.  So what is happening here is we've registered
 * the Hero class to the root of the json.  The {@link flexjson.JSONDeserializer#use(String, Class)} method  uses
 * the object graph path to attach certain classes to those locations.  So, when the deserializer is deserializing
 * it knows where it is in the object graph.  It uses that graph path to look up the java class it should use
 * when reconstituting the object.
 * </p>
 * <p>
 * Notice that in our json you'd see there is no type information in the stream.  However, all we had to do is point
 * the class at the Hero object, and it figured it out.  That's because it uses the target type (in this case Hero)
 * to figure out the other types by inspecting that class.  Meaning notice that we didn't have to tell it about
 * SecretLair or SecretIdentity.  That's because it can figure that out from the Hero class.
 * </p>
 * <p>
 * Pretty cool.  Where this fails is when we starting working with interfaces, abstract classes, and subclasses.
 * Yea our friend polymorphism can be a pain when deserializing.  Why?  Well if you haven't realized by now
 * inspecting the type from our target class won't help us because either it's not a concrete class or we
 * can't tell the subclass by looking at the super class alone.  Next section we're going to stand up on our
 * bare back horse.  Ready?  Let's do it.
 * </p>
 * <p>
 * Before we showed how the {@link flexjson.JSONDeserializer#use(String, Class)} method would allow us to
 * plug in a single class for a given path.  That might work when you know exactly which class you want to
 * instantiate, but when the class type depends on external factors we really need a way to specify several
 * possibilities.  That's where the second version of {@link flexjson.JSONDeserializer#use(String, ClassLocator)}
 * comes into play.  {@link flexjson.ClassLocator} allow you to use a stradegy for finding which java Class
 * you want to attach at a particular object path.
 * </p>
 * <p>
 * {@link flexjson.JSONDeserializer#use(String, ClassLocator)} have access to the intermediate form of
 * the object as a Map.  Given the Map at the object path the ClassLocator figures out which Class
 * Flexjson will bind the parameters into that object.
 * </p>
 * <p>
 * Let's take a look at how this can be done using our Hero class.  All Heros have a list of super powers.
 * These super powers are things like X Ray Vision, Heat Vision, Flight, etc.  Each super power is represented
 * by a subclass of SuperPower.  If we serialize a Hero without class information embedded we'll need a way to
 * figure out which instance to instantiate when we deserialize.  In this example I'm going to use a Transformer
 * during serialization to embed a special type information into the object.  All this transformer does is strip
 * off the package information on the class property.
 * </p>
 * <pre>
 * String json = new JSONSerializer()
 *      .include("powers.class")
 *      .transform( new SimpleTransformer(), "powers.class")
 *      .exclude("*.class")
 *      .serialize( superhero );
 * Hero hero = new JSONDeserializer<Hero>()
 *      .use("powers.class", new PackageClassLocator())
 *      .deserialize( json );
 * </pre>
 * <p>
 *
 * </p>
 * <p>
 * All objects that pass through the deserializer must have a no argument constructor.  The no argument
 * constructor does not have to be public.  That allows you to maintain some encapsulation.  JSONDeserializer
 * will bind parameters using setter methods of the objects instantiated if available.  If a setter method
 * is not available it will using reflection to set the value directly into the field.  You can use setter
 * methods transform the any data from json into the object structure you want.  That way json structure
 * can be different from your Java object structure.  The works very much in the same way getters do for
 * the {@link flexjson.JSONSerializer}.
 * </p>
 */
public class JSONDeserializer<T> {

    private ObjectBinder binder;

    public JSONDeserializer() {
        binder = new ObjectBinder();
    }

    public T deserialize( String input ) {
        return (T)binder.bind( new JSONTokener( input ).nextValue() );
    }

    public JSONDeserializer<T> use( String path, ClassLocator locator ) {
        binder.use( path, locator );
        return this;
    }

    public JSONDeserializer<T> use( String path, Class clazz ) {
        binder.use( path, clazz );
        return this;
    }
}
