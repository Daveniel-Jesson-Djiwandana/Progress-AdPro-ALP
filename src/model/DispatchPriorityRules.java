package model;

public class DispatchPriorityRules {
    private double victimWeight;
    private double areaWeight;
    private double intensityWeight;

    // Default weights as described in the spec
    public DispatchPriorityRules() {
        this.victimWeight    = 10.0;
        this.areaWeight      = 0.5;
        this.intensityWeight = 5.0;
    }

    public double calculate(int victims, double area, int intensity) {
        return (victimWeight * victims)
             + (areaWeight * area / 10.0)
             + (intensityWeight * intensity);
    }

    public double getVictimWeight()    { return victimWeight; }
    public double getAreaWeight()      { return areaWeight; }
    public double getIntensityWeight() { return intensityWeight; }

    public void setVictimWeight(double w)    { this.victimWeight = w; }
    public void setAreaWeight(double w)      { this.areaWeight = w; }
    public void setIntensityWeight(double w) { this.intensityWeight = w; }

    public void reset() {
        victimWeight    = 10.0;
        areaWeight      = 0.5;
        intensityWeight = 5.0;
    }
}
