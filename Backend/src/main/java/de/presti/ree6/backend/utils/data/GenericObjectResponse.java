package de.presti.ree6.backend.utils.data;

/**
 * A generic response object.
 * @param success if the request was successful.
 * @param object an object that should be contained in the response.
 * @param message the message of the response.
 * @param <R> the type of the object.
 */
public record GenericObjectResponse<R>(boolean success, R object, String message) {
}
