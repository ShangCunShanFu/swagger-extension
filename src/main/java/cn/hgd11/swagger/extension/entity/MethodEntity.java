package cn.hgd11.swagger.extension.entity;

import java.util.Objects;

/**************************************
 * Copyright (C), Navinfo
 * Package: 
 * @author: 尚村山夫
 * @date: Created in 2019/8/15 15:42
 * @description:
 **************************************/
public class MethodEntity {
    private String uri;
    private String requestMethod;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MethodEntity that = (MethodEntity) o;
        return Objects.equals(uri, that.uri) && Objects.equals(requestMethod, that.requestMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, requestMethod);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
}
