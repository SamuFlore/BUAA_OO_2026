package Factor;

import java.math.BigInteger;

public class Number implements Factor {

    private BigInteger num;

    public Number(String num) {
        this.num = new BigInteger(num);
    }

    @Override
    public String toString() {
        return num.toString();
    }

    @Override
    public Factor derive(String var) {
        Term term = new Term();
        Number zero = new Number("0");
        term.addFactor(zero);
        return term;
    }

    @Override
    public Factor clone() {
        return new Number(num.toString());
    }
}
