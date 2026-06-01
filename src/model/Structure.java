package model;

/**
 * Represents the physical building/location where a fire incident occurs.
 * Appears in class diagram as "Structure".
 */
public class Structure {
    private static int counter = 1;

    private String structureID;
    private String location;     // address string (diagram: int = zone code, kept as String for usability)
    private int    area;         // area size in m²
    private int    civilian_count; // number of people inside

    public Structure(String location, int area, int civilian_count) {
        this.structureID    = "STR-" + String.format("%04d", counter++);
        this.location       = location;
        this.area           = area;
        this.civilian_count = civilian_count;
    }

    public String getStructureID()          { return structureID; }
    public String getLocation()             { return location; }
    public void   setLocation(String location) { this.location = location; }
    public int    getArea()                 { return area; }
    public int    getCivilianCount()        { return civilian_count; }
    public void   setCivilianCount(int n)   { civilian_count = n; }
    public void   setArea(int a)            { area = a; }
}
