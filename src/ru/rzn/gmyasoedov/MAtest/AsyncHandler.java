package ru.rzn.gmyasoedov.MAtest;

/**
 * interface for handle response from request
 */
public interface AsyncHandler<T> {
    /**
     * handle success result
     * @param result response
     */
    void onSuccess(T result);

    /**
     * handle exception
     * @param e
     */
    void onError(Exception e);
}
