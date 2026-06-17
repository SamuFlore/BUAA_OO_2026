import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

// Term = x ^ exponent * exp(expContent), coeff is managed in HashMap
public class Term {
    private BigInteger exponent;
    private Expr expContent;

    public BigInteger getExponent() { return this.exponent; }

    public Expr getExpContent() { return this.expContent; }

    public Term(BigInteger exponent, Expr expContent) {
        this.exponent = (exponent == null) ? BigInteger.ZERO : exponent;
        this.expContent = (expContent == null) ? new Expr() : expContent;
    }

    public Term termMul(Term other) {
        BigInteger newExponent = this.exponent.add(other.exponent);
        Expr newExpContent = this.expContent.exprAdd(other.expContent);
        return new Term(newExponent, newExpContent);
    }

    // coeff * x ^ exponent * exp(expContent)
    public String termToString(BigInteger coeff) {
        if (coeff.equals(BigInteger.ZERO)) { return ""; } // skip
        if (exponent.equals(BigInteger.ZERO) && expContent.isEmpty()) { return coeff.toString(); }

        StringBuilder sb = new StringBuilder();
        // 系数，处理首项正负
        if (coeff.equals(BigInteger.ONE)) { /* 正号已经在 Expr 中添加 */ }
        else if (coeff.equals(BigInteger.valueOf(-1))) { sb.append("-"); }
        else {
            sb.append(coeff);
            sb.append("*");
        }
        // x^exponent
        boolean hasX = false;
        if (!exponent.equals(BigInteger.ZERO)) {
            hasX = true;
            if (exponent.equals(BigInteger.ONE)) { sb.append("x"); }
            else { sb.append("x^").append(exponent); }
        }
        // exp(expContent)
        if (!expContent.isEmpty()) {
            if (hasX) { sb.append("*"); }
            String basicExp = "exp(";
            if (expContent.isFactor()) {
                basicExp += expContent.toString() + ")";
            }
            else {
                basicExp += "(" + expContent.toString() + "))";
            }
            // Find GCD
            BigInteger gcd = BigInteger.ZERO;
            for (BigInteger c : expContent.getTerms().values()) {
                gcd = gcd.gcd(c.abs());
            }
            // if gcd exists and expContent has more than one terms
            if (gcd.compareTo(BigInteger.ONE) > 0 && expContent.getTerms().size() > 1) {
                Expr simpExpContent = new Expr();
                for (Map.Entry<Term, BigInteger> entry : expContent.getTerms().entrySet()) {
                    Term originTerm = entry.getKey();
                    BigInteger originCoeff = entry.getValue();
                    BigInteger simpCoeff = originCoeff.divide(gcd);
                    simpExpContent.addTerm(originTerm, simpCoeff);
                }
                String simpExp = "exp(";
                if (simpExpContent.isFactor()) {
                    simpExp += simpExpContent.toString() + ")^" + gcd.toString();
                }
                else {
                    simpExp += "(" + simpExpContent.toString() + "))^" + gcd.toString();
                }
                // check length
                if (simpExp.length() < basicExp.length()) {
                    sb.append(simpExp);
                }
                else { sb.append(basicExp); }
            }
            else { sb.append(basicExp); }
        }
        return sb.toString();
    }

    // Replace all x with arg: arg ^ n * exp(...)
    public Expr replace(Expr arg) {
        Expr var; // x^exponent
        if (this.exponent.equals(BigInteger.ZERO)) {
            var = Expr.createConst(BigInteger.ONE); // var ^ 0 = 1
        }
        else {
            var = arg.exprPow(this.exponent.intValue());
        }

        Expr exp; // exp(...)
        if (this.expContent.isEmpty()) {
            exp = Expr.createConst(BigInteger.ONE); // exp(0) = 1
        }
        else {
            Expr newExpContent = this.expContent.replace(arg);
            if (newExpContent.isEmpty()) {
                exp = Expr.createConst(BigInteger.ONE); // exp(0) = 1
            }
            else {
                exp = Expr.createExp(newExpContent);
            }
        }
        return var.exprMul(exp); // term = var * exp
    }

    @Override
    // exponent and expContent are the only condition
    public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof Term)) { return false; }
        Term term = (Term) other;
        return Objects.equals(exponent, term.exponent) &&
                Objects.equals(expContent, term.expContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exponent, expContent);
    }
}
