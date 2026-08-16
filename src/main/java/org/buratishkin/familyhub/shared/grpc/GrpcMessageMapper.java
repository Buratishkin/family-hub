package org.buratishkin.familyhub.shared.grpc;

import org.buratishkin.familyhub.address.api.AddressView;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.member.api.MemberView;
import org.buratishkin.familyhub.grpc.AddressViewMessage;
import org.buratishkin.familyhub.grpc.MemberViewMessage;
import org.buratishkin.familyhub.grpc.UserViewMessage;
import org.springframework.stereotype.Component;

@Component
public class GrpcMessageMapper {
    public UserViewMessage toMessage(UserView view) {
        return UserViewMessage.newBuilder()
                .setId(valueOrZero(view.id()))
                .setUsername(valueOrEmpty(view.username()))
                .setEmail(valueOrEmpty(view.email()))
                .build();
    }

    public UserView toUserView(UserViewMessage message) {
        return new UserView(
                message.getId(),
                nullIfEmpty(message.getUsername()),
                nullIfEmpty(message.getEmail())
        );
    }

    public MemberViewMessage toMessage(MemberView view) {
        return MemberViewMessage.newBuilder()
                .setId(valueOrZero(view.id()))
                .setFamilyId(valueOrZero(view.familyId()))
                .setUserId(valueOrZero(view.userId()))
                .setUsername(valueOrEmpty(view.username()))
                .setRole(view.role() == null ? "" : view.role().name())
                .setDefaultName(valueOrEmpty(view.defaultName()))
                .build();
    }

    public MemberView toMemberView(MemberViewMessage message) {
        return new MemberView(
                message.getId(),
                message.getFamilyId(),
                message.getUserId(),
                nullIfEmpty(message.getUsername()),
                nullIfEmpty(message.getRole()) == null ? null : org.buratishkin.familyhub.family.member.enums.MemberRole.valueOf(message.getRole()),
                nullIfEmpty(message.getDefaultName()),
                null
        );
    }

    public AddressViewMessage toMessage(AddressView view) {
        return AddressViewMessage.newBuilder()
                .setId(valueOrZero(view.id()))
                .setFamilyId(valueOrZero(view.familyId()))
                .setCategoryId(valueOrZero(view.categoryId()))
                .setName(valueOrEmpty(view.name()))
                .setCountry(valueOrEmpty(view.country()))
                .setCity(valueOrEmpty(view.city()))
                .setStreetType(valueOrEmpty(view.streetType()))
                .setStreet(valueOrEmpty(view.street()))
                .setHouse(valueOrEmpty(view.house()))
                .setApartment(valueOrEmpty(view.apartment()))
                .setComment(valueOrEmpty(view.comment()))
                .build();
    }

    public AddressView toAddressView(AddressViewMessage message) {
        return new AddressView(
                message.getId(),
                message.getFamilyId(),
                zeroToNull(message.getCategoryId()),
                nullIfEmpty(message.getName()),
                nullIfEmpty(message.getCountry()),
                nullIfEmpty(message.getCity()),
                nullIfEmpty(message.getStreetType()),
                nullIfEmpty(message.getStreet()),
                nullIfEmpty(message.getHouse()),
                nullIfEmpty(message.getApartment()),
                nullIfEmpty(message.getComment())
        );
    }

    private long valueOrZero(Long value) {
        return value == null ? 0 : value;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String nullIfEmpty(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Long zeroToNull(long value) {
        return value == 0 ? null : value;
    }
}
