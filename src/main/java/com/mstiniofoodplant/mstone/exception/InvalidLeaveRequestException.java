package com.mstiniofoodplant.mstone.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidLeaveRequestException extends RuntimeException {

    public InvalidLeaveRequestException(String message) {
        super(message);
    }

    public InvalidLeaveRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
