package de.presti.ree6.backend.utils.data.container.api;

/**
 * Request for the Punishments.
 * @param neededWarnings Needed Warnings of the Punishments.
 * @param action Action of the Punishments.
 * @param roleId Role ID of the Punishments.
 * @param timeoutTime Timeout Time of the Punishments.
 */
public record PunishmentsRequest(String neededWarnings, String action, String roleId, String timeoutTime) {
}
