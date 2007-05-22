package flexjson;

import java.util.Iterator;
import java.util.Set;

public class ChainedIterator implements Iterator {

    Iterator[] iterators;
    int current = 0;

    public ChainedIterator(Set... sets) {
        iterators = new Iterator[sets.length];
        for( int i = 0; i < sets.length; i++ ) {
            iterators[i] = sets[i].iterator();
        }
    }

    public boolean hasNext() {
        if( iterators[current].hasNext() ) {
            return true;
        } else {
            current++;
            return current < iterators.length && iterators[current].hasNext();
        }
    }


    public Object next() {
        return iterators[current].next();
    }

    public void remove() {
        iterators[current].remove();
    }
}
