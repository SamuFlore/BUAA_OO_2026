import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

public class Poly {
    // Key: exp, Value：coeff
    private TreeMap<BigInteger, BigInteger> terms;

    public Poly() {
        this.terms = new TreeMap<>();
    }

    // 向多项式中添加一项
    public void addTerm(BigInteger exp, BigInteger coeff) {
        if (coeff.equals(BigInteger.ZERO)) { return; } // 系数为 0，忽略
        BigInteger newCoeff = terms.getOrDefault(exp, BigInteger.ZERO).add(coeff); // 同指数项系数相加
        if (newCoeff.equals(BigInteger.ZERO)) {
            terms.remove(exp); // 系数归零，移除
        }
        else {
            terms.put(exp, newCoeff);
        }
    }

    // this + other
    public Poly add(Poly other) {
        Poly result = new Poly();
        this.terms.forEach(result::addTerm);
        other.terms.forEach(result::addTerm);
        return result;
    }

    // this - other
    public Poly sub(Poly other) {
        Poly result = new Poly();
        this.terms.forEach(result::addTerm);
        other.terms.forEach((exp, coeff) -> result.addTerm(exp, coeff.negate()));
        return result;
    }

    // this * other
    public Poly mul(Poly other) {
        Poly result = new Poly();
        for (Map.Entry<BigInteger, BigInteger> polies1 : this.terms.entrySet()) {
            for (Map.Entry<BigInteger, BigInteger> polies2 : other.terms.entrySet()) {
                // exp + exp
                BigInteger newExp = polies1.getKey().add(polies2.getKey());
                //　coeff * coeff
                BigInteger newCoeff = polies1.getValue().multiply(polies2.getValue());
                result.addTerm(newExp, newCoeff);
            }
        }
        return result;
    }

    // this ^ exp
    public Poly pow(int exp) {
        Poly result = new Poly();
        result.addTerm(BigInteger.ZERO, BigInteger.ONE); // 1*x^0
        for (int i = 0; i < exp; i++) {
            result = result.mul(this); // this ^ exp
        }
        return result;
    }

    @Override
    public String toString() {
        if (terms.isEmpty()) { return "0"; }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<BigInteger, BigInteger> polies : terms.entrySet()) {
            BigInteger exp = polies.getKey();
            BigInteger coeff = polies.getValue();
            if (coeff.signum() > 0 && (sb.length() > 0)) { // 如果是正数且不是第一个项
                sb.append("+");
            }

            // 特判：指数为 0
            if (exp.equals(BigInteger.ZERO)) {
                sb.append(coeff);
            } else {
                // 系数
                if (coeff.equals(BigInteger.ONE)) {
                    // 系数为 1：不写系数
                } else if (coeff.equals(BigInteger.valueOf(-1))) {
                    // 系数为 -1：只写负号
                    sb.append("-");
                } else {
                    // 普通系数：写 "系数*"
                    sb.append(coeff).append("*");
                }

                //  指数
                if (exp.equals(BigInteger.ONE)) {
                    sb.append("x"); // 省略 ^1
                } else {
                    sb.append("x^").append(exp);
                }
            }
        }
        return sb.toString();
    }
}
