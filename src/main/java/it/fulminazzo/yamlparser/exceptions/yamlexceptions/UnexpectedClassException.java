package it.fulminazzo.yamlparser.exceptions.yamlexceptions;

import it.fulminazzo.yamlparser.enums.LogMessage;

/**
 * This exception occurs when loading an object
 * from an IConfiguration, but the result does not
 * correspond to the expected type.
 */
public class UnexpectedClassException extends YAMLException {

    public UnexpectedClassException(String path, String name, Object object, String expected) {
        super(path, name, object, LogMessage.UNEXPECTED_CLASS.getMessage(
                "%expected%", expected,
                "%received%", object == null ? "null" : object.getClass().getSimpleName()
        ));
    }
}
