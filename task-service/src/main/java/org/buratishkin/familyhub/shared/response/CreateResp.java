package org.buratishkin.familyhub.shared.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CreateResp<T> {
    private boolean result;
    private String reason;
    private T createResp;
}
