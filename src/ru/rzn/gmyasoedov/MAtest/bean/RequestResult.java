package ru.rzn.gmyasoedov.MAtest.bean;

/**
 * Request bean. contain response or error
 */
public class RequestResult {
    private String response;
    private Exception exception;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
