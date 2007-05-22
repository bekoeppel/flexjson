package flexjson;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;

public class ChainedSet implements Set {
    Set parent;
    Set child;

    public ChainedSet(Set parent) {
        this.parent = parent;
        this.child = new HashSet();
    }

    public int size() {
        return this.child.size() + parent.size();
    }

    public boolean isEmpty() {
        return this.child.isEmpty() && parent.isEmpty();
    }

    public boolean contains(Object o) {
        return child.contains(o) || parent.contains(o);
    }

    public Iterator iterator() {
        return new ChainedIterator( child, parent );
    }

    public Object[] toArray() {
        Object[] carr = child.toArray();
        Object[] parr = parent.toArray();
        Object[] combined = new Object[ carr.length + parr.length ];
        System.arraycopy( carr, 0, combined, 0, carr.length );
        System.arraycopy( parr, 0, combined, carr.length, parr.length );
        return combined;
    }

    public Object[] toArray(Object[] a) {
        throw new IllegalStateException( "Not implemeneted" );
    }

    public boolean add(Object o) {
        return child.add( o );
    }

    public boolean remove(Object o) {
        return child.remove( o );
    }

    public boolean containsAll(Collection c) {
        return child.containsAll(c) || parent.containsAll(c); 
    }

    public boolean addAll(Collection c) {
        return child.addAll( c );
    }

    public boolean retainAll(Collection c) {
        return child.retainAll( c );
    }

    public boolean removeAll(Collection c) {
        return child.removeAll( c );
    }

    public void clear() {
        child.clear();
    }

    public Set getParent() {
        return parent;
    }
}
