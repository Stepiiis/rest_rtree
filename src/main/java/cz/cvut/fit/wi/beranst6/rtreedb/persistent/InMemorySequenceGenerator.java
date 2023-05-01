package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import org.springframework.stereotype.Service;

@Service
public class InMemorySequenceGenerator implements SequenceGeneratorInterface {

    int counter = 0;
    @Override
    public int getAndIncrease() {
        return counter++;
    }

    @Override
    public int getCurrentValue() {
        return counter;
    }

    @Override
    public void setCurrentValue(int value) {
        counter = value;
    }
}
