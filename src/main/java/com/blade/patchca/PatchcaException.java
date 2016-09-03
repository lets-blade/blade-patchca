package com.blade.patchca;

import com.blade.exception.BladeException;

public class PatchcaException extends BladeException {

    public PatchcaException() {
    }

    public PatchcaException(String message, Throwable cause) {
        super(message, cause);
    }

    public PatchcaException(String message) {
        super(message);
    }

    public PatchcaException(Throwable cause) {
        super(cause);
    }
}
