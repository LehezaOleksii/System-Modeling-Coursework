package oleksii.leheza.kpi.ms.v1.enums;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum AllowedProcessRequestType {

    FIRST_TYPE(RequestType.A, RequestType.B),
    SECOND_TYPE(RequestType.C);

    private final Set<RequestType> allowedRequestTypes;

    AllowedProcessRequestType(RequestType... allowedRequestTypes) {
        this.allowedRequestTypes = new HashSet<>(List.of(allowedRequestTypes));
    }

    public Set<RequestType> getAllowedRequestTypes() {
        return allowedRequestTypes;
    }
}
