package flexjson;

import java.util.List;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: charlie
 * Date: Jun 27, 2007
 * Time: 11:40:52 PM
 */
public class Path {
    LinkedList<String> path = new LinkedList<String>();

    public Path() {
    }

    public Path( String... fields ) {
        for (String field : fields) {
            path.add(field);
        }
    }

    public Path enqueue( String field ) {
        path.add( field );
        return this;
    }

    public String pop() {
        return path.removeLast();
    }

    public List<String> getPath() {
        return path;
    }

    public int length() {
        return path.size();
    }
}
