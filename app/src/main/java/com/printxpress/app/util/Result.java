package com.printxpress.app.util;

public final class Result<T> {
    private final T value;
    private final Exception error;

    private Result(T value, Exception error) {
        this.value = value;
        this.error = error;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> failure(Exception error) {
        return new Result<>(null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }

    public T getValue() {
        return value;
    }

    public Exception getError() {
        return error;
    }

    public String getErrorMessage() {
        return error == null ? null : error.getMessage();
    }
}
