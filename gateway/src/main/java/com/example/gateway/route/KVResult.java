package com.example.gateway.route;

public class KVResult {

    private String Key;
    private String Value;
    private Long ModifyIndex;

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public Long getModifyIndex() {
        return ModifyIndex;
    }

    public void setModifyIndex(Long modifyIndex) {
        ModifyIndex = modifyIndex;
    }
}