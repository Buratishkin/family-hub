package org.buratishkin.familyhub.integration.grpc.server;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.api.FamilyAccessApi;
import org.buratishkin.familyhub.grpc.BooleanResponse;
import org.buratishkin.familyhub.grpc.CurrentUserRequest;
import org.buratishkin.familyhub.grpc.FamilyAccessPortServiceGrpc;
import org.buratishkin.familyhub.grpc.FamilyAccessRequest;
import org.buratishkin.familyhub.grpc.UserViewMessage;
import org.buratishkin.familyhub.shared.grpc.GrpcErrors;
import org.buratishkin.familyhub.shared.grpc.GrpcMessageMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FamilyAccessGrpcService extends FamilyAccessPortServiceGrpc.FamilyAccessPortServiceImplBase {
    private final FamilyAccessApi familyAccessApi;
    private final GrpcMessageMapper mapper;

    @Override
    public void currentUser(CurrentUserRequest request, StreamObserver<UserViewMessage> responseObserver) {
        try {
            responseObserver.onNext(mapper.toMessage(familyAccessApi.currentUser(request.getUsername())));
            responseObserver.onCompleted();
        } catch (RuntimeException exception) {
            responseObserver.onError(GrpcErrors.notFound(exception));
        }
    }

    @Override
    public void hasFamilyAccess(FamilyAccessRequest request, StreamObserver<BooleanResponse> responseObserver) {
        responseObserver.onNext(BooleanResponse.newBuilder()
                .setValue(familyAccessApi.hasFamilyAccess(request.getUserId(), request.getFamilyId()))
                .build());
        responseObserver.onCompleted();
    }
}
