package domain.service;

import domain.model.Session;
import patterns.OrganizerSubject;

import java.util.Map;

public class OrganizerService {
    private final Map<Integer, Session> sessions;       // sessionId -> Session
    private final OrganizerSubject organizerSubject;    // Subject for notifying attendees

    public OrganizerService(Map<Integer, Session> sessions, OrganizerSubject organizerSubject) {
        this.sessions = sessions;
        this.organizerSubject = organizerSubject;
    }

    /**
     * Cancel a session and notify all attendees about the cancellation.
     * @param sessionId ID of the session to cancel
     * @param reason Reason for cancellation (e.g., "Speaker unavailable", "Venue issues")
     * @return true if the session was successfully cancelled, false if the session does not exist
     */
    public boolean cancelSession(int sessionId, String reason) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return false;
        }

        session.cancel();
        organizerSubject.notifyUpdate(sessionId, reason, true);
        return true;
    }


    /**
     * Update the capacity of a session and notify attendees about the change.
     * @param sessionId ID of the session to update
     * @param newCapacity New capacity for the session
     * @param reason Reason for capacity change (e.g., "Room change", "Increased demand")
     * @return true if the capacity was successfully updated, false if the session does not exist or if the new capacity is invalid
     */
    public boolean updateSessionCapacity(int sessionId, int newCapacity, String reason) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return false;
        }

        boolean updated = session.updateCapacity(newCapacity);
        if (updated) {
            organizerSubject.notifyUpdate(
                    sessionId,
                    reason + " | new capacity=" + newCapacity,
                    false
            );
        }

        return updated;
    }
}