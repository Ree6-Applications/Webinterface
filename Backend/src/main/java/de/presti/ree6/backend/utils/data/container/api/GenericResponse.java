package de.presti.ree6.backend.utils.data.container.api;

/**
 * A generic response object.
 * @param success if the request was successful.
 * @param message the message of the response.
 */
public record GenericResponse(boolean success, String message) {}