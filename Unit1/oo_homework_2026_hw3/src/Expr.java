import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

// Expr = Term + Term + ...
public class Expr {
    // BigInt: coeff, Term: x^exponent*exp(expContent), use Term to check whether they are equal.
    private final HashMap<Term, BigInteger> terms; // K: Term, V: Coeff

    public Expr() {
        this.terms = new HashMap<>();
    }

    public HashMap<Term, BigInteger> getTerms() { return terms; }

    // Term: x^exponent*exp(expContent)
    public void addTerm(Term term, BigInteger coeff) {
        if (coeff.equals(BigInteger.ZERO)) { return; }
        BigInteger curCoeff = terms.getOrDefault(term, BigInteger.ZERO);
        BigInteger newCoeff = curCoeff.add(coeff);
        if (newCoeff.equals(BigInteger.ZERO)) { terms.remove(term); }
        else { terms.put(term, newCoeff); }
    }

    public boolean isEmpty() {
        return terms.isEmpty();
    }

    public boolean isFactor() {
        if (terms.size() != 1) { return false; } // 项数不为 1
        // 项数必定为 1
        Map.Entry<Term, BigInteger> entry = terms.entrySet().iterator().next();
        Term term = entry.getKey();
        BigInteger coeff = entry.getValue();
        // 如果是常数：coeff * x ^ 0 * y ^ 0 * exp(0)
        if (term.getExponent().equals(BigInteger.ZERO) &&
                term.getExponentY().equals(BigInteger.ZERO) &&
                term.getExpContent().isEmpty()) {
            return true;
        }
        // 如果是 1 * x ^ n * y ^ 0 * exp(0) 或
        // 1 * x ^ 0 * y ^ m * exp(0) 或
        // 1 * x ^ 0 * y ^ 0 * exp(expContent)
        if (coeff.equals(BigInteger.ONE)) {
            int n = term.getExponent().signum();
            int m = term.getExponentY().signum();
            Expr expContent = term.getExpContent();
            boolean isXExpr = n > 0 && m == 0 && expContent.isEmpty();
            boolean isYExpr = m > 0 && n == 0 && expContent.isEmpty();
            boolean isExpExpr = n == 0 && m == 0 && !expContent.isEmpty();
            return isXExpr || isYExpr || isExpExpr;
        }
        return false;
    }

    // Expr1 + Expr2:
    public Expr exprAdd(Expr other) {
        Expr res = new Expr();
        this.terms.forEach(res::addTerm);
        other.terms.forEach(res::addTerm);
        return res;
    }

    // Expr1 - Expr2:
    public Expr exprSub(Expr other) {
        Expr res = new Expr();
        this.terms.forEach(res::addTerm);
        other.terms.forEach((term, coeff) -> res.addTerm(term, coeff.negate()));
        return res;
    }

    // Expr1 * Expr2:
    public Expr exprMul(Expr other) {
        Expr res = new Expr();
        // one expr == empty, return empty
        if (this.terms.isEmpty() || other.terms.isEmpty()) { return res; }
        for (Map.Entry<Term, BigInteger> expr1 : this.terms.entrySet()) {
            for (Map.Entry<Term, BigInteger> expr2 : other.terms.entrySet()) {
                Term term1 = expr1.getKey();
                BigInteger coeff1 = expr1.getValue();
                Term term2 = expr2.getKey();
                BigInteger coeff2 = expr2.getValue();
                Term newTerm = term1.termMul(term2);
                BigInteger newCoeff = coeff1.multiply(coeff2);
                res.addTerm(newTerm, newCoeff);
            }
        }
        return res;
    }

    // Expr ^ exponent
    public Expr exprPow(int exponent) {
        Expr res = new Expr();
        if (exponent == 0) {
            // 1*x^0*y^0*exp(0)
            res.addTerm(new Term(BigInteger.ZERO, BigInteger.ZERO, new Expr()), BigInteger.ONE);
            return res;
        }
        res = this;
        for (int i = 1; i < exponent; i++) {
            res = res.exprMul(this);
        }
        return res;
    }

    // take derivative of var
    public Expr exprDeriv(String var) {
        Expr res = new Expr();
        for (Map.Entry<Term, BigInteger> expr : this.terms.entrySet()) {
            Term term = expr.getKey();
            BigInteger coeff = expr.getValue();
            Expr termDeriv = term.termDeriv(var);
            // coeff * termDeriv + coeff * termDeriv ...
            res = res.exprAdd(termDeriv.exprMul(createConst(coeff)));
        }
        return res;
    }

    // coeff * x ^ 0 * y ^ 0 * exp(0), public method
    public static Expr createConst(BigInteger coeff) {
        Expr res = new Expr();
        res.addTerm(new Term(BigInteger.ZERO, BigInteger.ZERO, new Expr()), coeff);
        return res;
    }

    // 1 * x ^ exponent * y ^ exponentY * exp(0)
    public static Expr createVar(BigInteger exponent, BigInteger exponentY) {
        Expr res = new Expr();
        res.addTerm(new Term(exponent, exponentY, new Expr()), BigInteger.ONE);
        return res;
    }

    // 1 * x ^ 0 * y ^ 0 * exp(expContent)
    public static Expr createExp(Expr expContent) {
        Expr res = new Expr();
        res.addTerm(new Term(BigInteger.ZERO, BigInteger.ZERO, expContent), BigInteger.ONE);
        return res;
    }

    public static Expr createTerm(Term term, BigInteger coeff) {
        Expr res = new Expr();
        res.addTerm(term, coeff);
        return res;
    }

    public Expr replace(Expr arg) {
        Expr res = new Expr();
        for (Map.Entry<Term, BigInteger> entry : this.terms.entrySet()) {
            Term term = entry.getKey();
            BigInteger coeff = entry.getValue();
            Expr replacedTerm = term.replace(arg);
            Expr newTerm = Expr.createConst(coeff).exprMul(replacedTerm);
            res = res.exprAdd(newTerm);
        }
        return res;
    }

    @Override
    public String toString() {
        if (this.terms.isEmpty()) { return "0"; }
        StringBuilder sb = new StringBuilder();
        Term head = null;
        for (Map.Entry<Term, BigInteger> entry : this.terms.entrySet()) {
            if (entry.getValue().signum() > 0) { // 找一个正项
                head =  entry.getKey();
                break;
            }
        }
        if (head != null) {
            sb.append(head.termToString(terms.get(head)));
        }
        for (Map.Entry<Term, BigInteger> expr : this.terms.entrySet()) {
            Term term = expr.getKey();
            BigInteger coeff = expr.getValue();
            // 跳过之前发现的
            if (term.equals(head)) { continue; }
            // 正，且不是第一个
            if (coeff.signum() > 0 && sb.length() > 0) {
                sb.append("+");
            }
            sb.append(term.termToString(coeff));
        }
        return sb.toString();
    }

    @Override
    // this.HashMap is equals to other.HashMap
    public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof Expr)) { return false; }
        Expr otherExpr = (Expr) other;
        // pre-check
        if (this.hashCode() != otherExpr.hashCode()) { return false; }
        // compare every elements
        return this.terms.equals(otherExpr.terms);
    }

    @Override
    public int hashCode() {
        return this.terms.hashCode();
    }

}
