package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

public interface SequenceGeneratorInterface {
    public int getAndIncrease();
    public int getCurrentValue();
    public void setCurrentValue(int value);
}
