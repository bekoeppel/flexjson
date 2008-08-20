package flexjson.model;

/**
 * Created by IntelliJ IDEA.
 * User: brandongoodin
 * Date: Aug 19, 2008
 * Time: 5:32:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoopClassTwo {

    private LoopClassOne loopClassOne;

    public LoopClassOne getLoopClassOne() {
        if(loopClassOne == null) {
            this.loopClassOne = new LoopClassOne();
        }
        return loopClassOne;
    }

    public void setLoopClassOne(LoopClassOne loopClassOne) {
        this.loopClassOne = loopClassOne;
    }
}
