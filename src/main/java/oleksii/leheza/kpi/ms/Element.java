package oleksii.leheza.kpi.ms;

abstract class Element {

    private static int generalId = 1;

    protected int id;
    protected String name;
    protected double currentTime;
    protected double nextEventTime;

    public Element(String name) {
        this.name = name;
        id = generalId;
        generalId++;
        nextEventTime = Double.MAX_VALUE;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }

    public double getNextEventTime() {
        return nextEventTime;
    }

    public void setNextEventTime(double time) {
        this.nextEventTime = time;
    }
}
