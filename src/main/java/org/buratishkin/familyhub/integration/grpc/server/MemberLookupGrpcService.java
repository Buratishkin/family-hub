package org.buratishkin.familyhub.integration.grpc.server;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.member.api.MemberLookupApi;
import org.buratishkin.familyhub.grpc.MemberByFamilyAndUserRequest;
import org.buratishkin.familyhub.grpc.MemberByIdRequest;
import org.buratishkin.familyhub.grpc.MemberListMessage;
import org.buratishkin.familyhub.grpc.MemberLookupPortServiceGrpc;
import org.buratishkin.familyhub.grpc.MemberViewMessage;
import org.buratishkin.familyhub.grpc.MembersByFamilyRequest;
import org.buratishkin.familyhub.shared.grpc.GrpcErrors;
import org.buratishkin.familyhub.shared.grpc.GrpcMessageMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberLookupGrpcService extends MemberLookupPortServiceGrpc.MemberLookupPortServiceImplBase {
    private final MemberLookupApi memberLookupApi;
    private final GrpcMessageMapper mapper;

    @Override
    public void findMemberById(MemberByIdRequest request, StreamObserver<MemberViewMessage> responseObserver) {
        try {
            responseObserver.onNext(mapper.toMessage(memberLookupApi.findMemberById(request.getMemberId())));
            responseObserver.onCompleted();
        } catch (RuntimeException exception) {
            responseObserver.onError(GrpcErrors.notFound(exception));
        }
    }

    @Override
    public void findMemberByFamilyIdAndUserId(MemberByFamilyAndUserRequest request, StreamObserver<MemberViewMessage> responseObserver) {
        try {
            responseObserver.onNext(mapper.toMessage(memberLookupApi.findMemberByFamilyIdAndUserId(request.getFamilyId(), request.getUserId())));
            responseObserver.onCompleted();
        } catch (RuntimeException exception) {
            responseObserver.onError(GrpcErrors.notFound(exception));
        }
    }

    @Override
    public void findMembersByFamilyId(MembersByFamilyRequest request, StreamObserver<MemberListMessage> responseObserver) {
        responseObserver.onNext(MemberListMessage.newBuilder()
                .addAllMembers(memberLookupApi.findMembersByFamilyId(request.getFamilyId()).stream()
                        .map(mapper::toMessage)
                        .toList())
                .build());
        responseObserver.onCompleted();
    }
}
