package Factor;

public class Var implements Factor {

    private String name;

    public Var(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Factor derive(String var) {
        Term term = new Term();
        if (this.name.equals(var)) {
            Number ans = new Number("1");
            term.addFactor(ans);
        }
        else {
            Number ans = new Number("0");
            term.addFactor(ans);
        }
        return term;
    }

    @Override
    public Factor clone() {
        return new Var(name);
    }
}
