package groupone.itiprj.com.e15.grp12.mapp;

/**
 * Class defining the map object
 */
public class Map {

    private long id;
    private String path;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }
}
