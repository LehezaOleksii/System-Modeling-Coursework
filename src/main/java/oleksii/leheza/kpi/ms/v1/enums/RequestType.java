package oleksii.leheza.kpi.ms.v1.enums;

public enum RequestType {

    A(20, 5, 20, 1),
    B(20, 10, 15, 3),
    C(30, 10, 15, 5);

    private final double arrivalTimeMean;
    private final double arrivalTimeVariance;
    private final double processingTimeMean;
    private final double processingTimeVariance;

    RequestType(double arrivalTimeMean, double arrivalTimeVariance, double processingTimeMean, double processingTimeVariance) {
        this.arrivalTimeMean = arrivalTimeMean;
        this.arrivalTimeVariance = arrivalTimeVariance;
        this.processingTimeMean = processingTimeMean;
        this.processingTimeVariance = processingTimeVariance;
    }

    public double getArrivalTimeMean() {
        return arrivalTimeMean;
    }

    public double getArrivalTimeVariance() {
        return arrivalTimeVariance;
    }

    public double getProcessingTimeMean() {
        return processingTimeMean;
    }

    public double getProcessingTimeVariance() {
        return processingTimeVariance;
    }
}

