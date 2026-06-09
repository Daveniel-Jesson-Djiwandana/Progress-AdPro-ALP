package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;

public class Incident implements Comparable<Incident> {
    private static int counter = 1;

    private Structure structure;
    private LocalDateTime fire_start_time;
    private int priority_level; // integer version of priority score

    private int id;
    private IncidentSeverity severity;
    private String description;
    private int fireIntensity; // 1–10
    private IncidentStatus status;
    private String reportedBy;
    private double priorityScore;
    private int victimsCritical;
    private int victimsInjured;
    private int victimsEvacuated;
    private int victimsSafe;
    private int trucksAssigned;

    // Bangunan
    private BuildingCategory buildingCategory; // kategori fungsi bangunan
    private String buildingSubType; // subtipe spesifik

    // Detail
    private BuildingMaterial buildingMaterial; // material dominan
    private DamageLevel damageLevel; // tingkat kerusakan

    // Progress
    private LocalDateTime dispatchStartTime; // when first truck was sent
    private int dispatchProgress; // 0–100 %, simulated handling progress

    public Incident(Structure structure, IncidentSeverity severity, String description,
            int fireIntensity, String reportedBy) {
        this.id = counter++;
        this.structure = structure;
        this.severity = severity;
        this.description = description;
        this.fireIntensity = fireIntensity;
        this.reportedBy = reportedBy;
        this.fire_start_time = LocalDateTime.now();
        this.status = IncidentStatus.REPORTED;
        this.victimsCritical = 0;
        this.victimsInjured = 0;
        this.victimsEvacuated = 0;
        this.victimsSafe = 0;
        this.trucksAssigned = 0;
        this.dispatchProgress = 0;
        this.dispatchStartTime = null;
        this.buildingCategory = null;
        this.buildingSubType = null;
        this.buildingMaterial = null;
        this.damageLevel = null;
        generateCivilians();
    }

    // auto generate civilians???
    private void generateCivilians() {
        Random rng = new Random();
        int count = structure.getCivilianCount();
        for (int i = 0; i < count; i++) {
            int roll = rng.nextInt(100);
            switch (severity) {
                case TRIPLE_RED:
                    if (roll < 40)
                        victimsCritical++;
                    else if (roll < 70)
                        victimsInjured++;
                    else
                        victimsSafe++;
                    break;
                case DOUBLE_RED:
                    if (roll < 10)
                        victimsCritical++;
                    else if (roll < 45)
                        victimsInjured++;
                    else
                        victimsSafe++;
                    break;
                case RED:
                    if (roll < 15)
                        victimsInjured++;
                    else
                        victimsSafe++;
                    break;
                case UNDETERMINED:
                default:
                    if (roll < 10)
                        victimsInjured++;
                    else
                        victimsSafe++;
                    break;
            }
        }
    }

    public void calculatePriorityScore(double victimWeight, double areaWeight, double intensityWeight) {
        double score = 0.0;
        if (buildingCategory == BuildingCategory.BANGUNAN) {
            // Bangunan: prioritas utama cari korban
            score = (victimWeight * 1.5 * structure.getCivilianCount())
                    + (areaWeight * structure.getArea() / 10.0)
                    + (intensityWeight * fireIntensity);
        } else if (buildingCategory == BuildingCategory.INDUSTRI) {
            // Industri: bahan berbahaya
            score = (victimWeight * structure.getCivilianCount())
                    + (areaWeight * structure.getArea() / 10.0)
                    + (intensityWeight * 1.5 * fireIntensity);
        } else if (buildingCategory == BuildingCategory.LAHAN_KOSONG) {
            // Lahan Kosong: area luas, api menyebar
            score = (victimWeight * structure.getCivilianCount())
                    + (areaWeight * 2.0 * structure.getArea() / 10.0)
                    + (intensityWeight * fireIntensity);
        } else {
            score = (victimWeight * structure.getCivilianCount())
                    + (areaWeight * structure.getArea() / 10.0)
                    + (intensityWeight * fireIntensity);
        }
        this.priorityScore = score;
        this.priority_level = (int) priorityScore;
    }

    @Override
    public int compareTo(Incident other) {
        return Double.compare(other.priorityScore, this.priorityScore); 
    }

    // Durasi

    // Jumlah detik
    public long getDurationSeconds() {
        return java.time.Duration.between(fire_start_time, LocalDateTime.now()).getSeconds();
    }

    // Formatting
    public String getFormattedDuration() {
        long secs = getDurationSeconds();
        long mins = secs / 60;
        long rem = secs % 60;
        if (mins == 0)
            return rem + " dtk";
        return mins + " mnt " + rem + " dtk";
    }

    // Dispatch
    public void startDispatch() {
        if (dispatchStartTime == null) {
            dispatchStartTime = LocalDateTime.now();
        }
    }

    // Progress
    public void advanceProgress(int amount) {
        dispatchProgress = Math.min(100, dispatchProgress + amount);
    }

    public String getIncidentId() {
        return "INC-" + String.format("%04d", id);
    }

    public String getFormattedTime() {
        return fire_start_time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public int getRecommendedTrucks() {
        if (priorityScore >= 100)
            return 4;
        if (priorityScore >= 60)
            return 3;
        if (priorityScore >= 30)
            return 2;
        return 1;
    }

    public String getLocation() {
        return structure.getLocation();
    }

    public int getNumVictimsTrapped() {
        return structure.getCivilianCount();
    }

    public double getFireSpreadArea() {
        return structure.getArea();
    }


    public int getId() {
        return id;
    }

    public Structure getStructure() {
        return structure;
    }

    public LocalDateTime getFireStartTime() {
        return fire_start_time;
    }

    public int getPriorityLevel() {
        return priority_level;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity s) {
        this.severity = s;
    }

    public String getDescription() {
        return description;
    }

    public int getFireIntensity() {
        return fireIntensity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus s) {
        this.status = s;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double d) {
        this.priorityScore = d;
    }

    public int getVictimsCritical() {
        return victimsCritical;
    }

    public void setVictimsCritical(int c) {
        this.victimsCritical = c;
    }

    public int getVictimsInjured() {
        return victimsInjured;
    }

    public void setVictimsInjured(int i) {
        this.victimsInjured = i;
    }

    public int getVictimsEvacuated() {
        return victimsEvacuated;
    }

    public void setVictimsEvacuated(int e) {
        this.victimsEvacuated = e;
    }

    public int getVictimsSafe() {
        return victimsSafe;
    }

    public void setVictimsSafe(int s) {
        this.victimsSafe = s;
    }

    public int getTrucksAssigned() {
        return trucksAssigned;
    }

    public void setTrucksAssigned(int n) {
        this.trucksAssigned = n;
    }

    public int getDispatchProgress() {
        return dispatchProgress;
    }

    public void setDispatchProgress(int p) {
        this.dispatchProgress = Math.min(100, Math.max(0, p));
    }

    public LocalDateTime getDispatchStartTime() {
        return dispatchStartTime;
    }

    // backward-compat alias
    public LocalDateTime getReportedAt() {
        return fire_start_time;
    }

    public BuildingCategory getBuildingCategory() {
        return buildingCategory;
    }

    public void setBuildingCategory(BuildingCategory c) {
        this.buildingCategory = c;
    }

    public String getBuildingSubType() {
        return buildingSubType;
    }

    public void setBuildingSubType(String s) {
        this.buildingSubType = s;
    }

    public BuildingMaterial getBuildingMaterial() {
        return buildingMaterial;
    }

    public void setBuildingMaterial(BuildingMaterial m) {
        this.buildingMaterial = m;
    }

    public DamageLevel getDamageLevel() {
        return damageLevel;
    }

    public void setDamageLevel(DamageLevel d) {
        this.damageLevel = d;
    }

    // Tampilan singkat
    public String getBuildingLabel() {
        if (buildingCategory == null)
            return "Tidak diketahui";
        if (buildingSubType == null || buildingSubType.isBlank())
            return buildingCategory.getLabel();
        return buildingSubType + " (" + buildingCategory.getLabel() + ")";
    }
}
