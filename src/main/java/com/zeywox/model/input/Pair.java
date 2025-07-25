package com.zeywox.model.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter

public class Pair {
    private String day;
    private int meetNode;

    @Override
    public String toString() {
        return new StringBuilder()
                .append("[day: ").append(day)
                .append(", meetNode: ").append(meetNode).append("]")
                .toString();
    }
}
