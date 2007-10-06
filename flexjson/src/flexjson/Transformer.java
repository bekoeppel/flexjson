package flexjson;

/**
 * Transformers are used to alter the values written to a Flexjson stream.
 * This allows you to modify your data for use with HTML, security like stripping
 * out &lt;script&gt; tags, or rendering HTML from simple markups like markdown or other
 * technologies.  Use {@link JSONSerializer#transform} to register a Transformer to with
 * a JSONSerializer.
 */
public interface Transformer {
    public String transform( Object value );
}
