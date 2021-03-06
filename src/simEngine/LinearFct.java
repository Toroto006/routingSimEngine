package simEngine;

public class LinearFct implements CostFct {
    private final Float b;
    private final Float a;

    public LinearFct(Float a, Float b){
        this.a = a;
        this.b = b;
    }


    @Override
    public Float getCost(int t) {
        return a*t + b;
    }

    public double getCost(double t) {
        return a*t + b;
    }

    public Float[] getParameters() {return new Float[]{a, b};}

    @Override
    public Float getDerivativeCost() {
        return a;
    }

    @Override
    public String toString() {
        return a + "*t+" + b;
    }

    public String toString(int withAgents) {
        return a + "*" + withAgents + "+" + b + " = " + this.getCost(withAgents);
    }
}
