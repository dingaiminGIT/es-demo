package com.ding.es.document;

import java.util.List;

public class OecpError {
    private String id;
    private String errorCode;
    private String errorMsg;
    private String errorDesc;
    private List<String> errorTag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public List<String> getErrorTag() {
        return errorTag;
    }

    public void setErrorTag(List<String> errorTag) {
        this.errorTag = errorTag;
    }
}
