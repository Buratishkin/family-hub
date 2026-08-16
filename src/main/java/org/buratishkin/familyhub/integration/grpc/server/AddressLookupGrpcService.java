package org.buratishkin.familyhub.integration.grpc.server;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.address.api.AddressLookupApi;
import org.buratishkin.familyhub.grpc.AddressByIdRequest;
import org.buratishkin.familyhub.grpc.AddressLookupPortServiceGrpc;
import org.buratishkin.familyhub.grpc.AddressViewMessage;
import org.buratishkin.familyhub.shared.grpc.GrpcErrors;
import org.buratishkin.familyhub.shared.grpc.GrpcMessageMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressLookupGrpcService extends AddressLookupPortServiceGrpc.AddressLookupPortServiceImplBase {
    private final AddressLookupApi addressLookupApi;
    private final GrpcMessageMapper mapper;

    @Override
    public void findAddressById(AddressByIdRequest request, StreamObserver<AddressViewMessage> responseObserver) {
        try {
            responseObserver.onNext(mapper.toMessage(addressLookupApi.findAddressById(request.getAddressId())));
            responseObserver.onCompleted();
        } catch (RuntimeException exception) {
            responseObserver.onError(GrpcErrors.notFound(exception));
        }
    }
}
