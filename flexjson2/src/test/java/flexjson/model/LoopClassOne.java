package flexjson.model;

/**
 * Created by IntelliJ IDEA.
 * User: brandongoodin
 * Date: Aug 19, 2008
 * Time: 5:32:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoopClassOne {

    private LoopClassTwo loopClassTwo;

    public LoopClassTwo getLoopClassTwo() {
        if(loopClassTwo == null) {
            this.loopClassTwo = new LoopClassTwo();
        }
        return loopClassTwo;
    }

    public void setLoopClassTwo(LoopClassTwo loopClassTwo) {
        this.loopClassTwo = loopClassTwo;
    }
}
