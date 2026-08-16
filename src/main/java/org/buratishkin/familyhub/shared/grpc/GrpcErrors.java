package org.buratishkin.familyhub.shared.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public final class GrpcErrors {
    private GrpcErrors() {
    }

    public static StatusRuntimeException notFound(RuntimeException exception) {
        return Status.NOT_FOUND
                .withDescription(exception.getMessage())
                .withCause(exception)
                .asRuntimeException();
    }
}
