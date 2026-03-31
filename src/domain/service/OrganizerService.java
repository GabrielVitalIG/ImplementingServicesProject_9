package domain.service;

import domain.model.Session;
import patterns.OrganizerSubject;

import java.util.Map;

public class OrganizerService {
    private final Map<Integer, Session> sessions;
    private final OrganizerSubject organizerSubject;

    public OrganizerService(Map<Integer, Session> sessions, OrganizerSubject organizerSubject) {
        this.sessions = sessions;
        this.organizerSubject = organizerSubject;
    }

    public boolean cancelSession(int sessionId, String reason) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return false;
        }

        session.cancel();
        organizerSubject.notifyUpdate(sessionId, reason, true);
        return true;
    }

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