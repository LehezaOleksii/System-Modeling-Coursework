package oleksii.leheza.kpi.ms.enums;

public enum RequestType {

    A(20, 5, 20, 1),
    B(20, 10, 15, 3),
    C(30, 10, 15, 5);

    private final int arrivalTimeMean;
    private final int arrivalTimeVariance;
    private final int processingTimeMean;
    private final int processingTimeVariance;

    RequestType(int arrivalTimeMean, int arrivalTimeVariance, int processingTimeMean, int processingTimeVariance) {
        this.arrivalTimeMean = arrivalTimeMean;
        this.arrivalTimeVariance = arrivalTimeVariance;
        this.processingTimeMean = processingTimeMean;
        this.processingTimeVariance = processingTimeVariance;
    }

    public int getArrivalTimeMean() {
        return arrivalTimeMean;
    }

    public int getArrivalTimeVariance() {
        return arrivalTimeVariance;
    }

    public int getProcessingTimeMean() {
        return processingTimeMean;
    }

    public int getProcessingTimeVariance() {
        return processingTimeVariance;
    }
}

