package org.buratishkin.familyhub.shared.mapper;

public interface Mapper<S, T> {
    T toEntity(S source);
}
