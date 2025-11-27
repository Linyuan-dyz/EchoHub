package com.nowcoder.community.domain.entity;

import lombok.Getter;

@Getter
public enum BusinessType {
    DISCUSS_POST(0),
    COMMENT(1);

    private final int value;

    BusinessType(int value) {
        this.value = value;
    }

}
