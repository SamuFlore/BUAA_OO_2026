package Factor;

public class Exp implements Factor {

    private Factor factor;

    public Exp(Factor factor) {
        this.factor = factor;
    }

    @Override
    public String toString() {
        return "exp(" + factor.toString() + ")";
    }

    @Override
    public Factor derive(String var) {
        Term term = new Term();
        term.addFactor(factor.derive(var));
        term.addFactor(this.clone());
        return term;
    }

    @Override
    public Factor clone() {
        return new Exp(factor.clone());
    }
}
