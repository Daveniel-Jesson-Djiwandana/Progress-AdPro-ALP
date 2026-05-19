package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Archived record of a resolved incident.
 * Diagram fields: incident, resource_used, timeSpent, description.
 */
public class ReportHistory {
    // ── Diagram fields ─────────────────────────────────────────────────────
    private Incident incident;
    private HashMap<Resource, Integer> resource_used; // required data structure
    private LocalDateTime timeSpent;                  // time of resolution
    private String description;                       // resolution notes

    // ── Extra operational fields ──────────────────────────────────────────
    private String resolvedBy;
    private int    trucksDeployed;

    public ReportHistory(Incident incident, String resolvedBy, int trucksDeployed, String notes) {
        this.incident      = incident;
        this.timeSpent     = LocalDateTime.now();
        this.resolvedBy    = resolvedBy;
        this.trucksDeployed = trucksDeployed;
        this.description   = notes;
        this.resource_used = new HashMap<>();
        estimateResources(incident.getSeverity(), trucksDeployed);
    }

    private void estimateResources(IncidentSeverity sev, int trucks) {
        int m = Math.max(1, trucks);
        switch (sev) {
            case MEDIUM:   m *= 2; break;
            case HIGH:     m *= 3; break;
            case CRITICAL: m *= 5; break;
            default: break;
        }
        resource_used.put(Resource.WATER,         500 * m);
        resource_used.put(Resource.FOAM,          50  * m);
        resource_used.put(Resource.HOSE,          2   * m);
        resource_used.put(Resource.OXYGEN_TANK,   m);
        resource_used.put(Resource.FIRST_AID_KIT, m);
    }

    public Incident  getIncident()                         { return incident; }
    public HashMap<Resource, Integer> getResourcesUsed()   { return resource_used; }
    public HashMap<Resource, Integer> getResourceUsed()    { return resource_used; }
    public LocalDateTime getTimeSpent()                    { return timeSpent; }
    public LocalDateTime getResolvedAt()                   { return timeSpent; } // compat
    public String getNotes()                               { return description; }
    public String getDescription()                         { return description; }
    public String getResolvedBy()                          { return resolvedBy; }
    public int    getTrucksDeployed()                      { return trucksDeployed; }
    public String getFormattedResolvedTime() {
        return timeSpent.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
