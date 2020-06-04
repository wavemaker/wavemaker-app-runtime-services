package com.wavemaker.runtime.connector.exception;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 2/6/20
 */
public class InvalidConnector extends RuntimeException {

    public InvalidConnector(String message) {
        super(message);
    }

    public InvalidConnector(String message, Throwable cause) {
        super(message, cause);
    }
}
