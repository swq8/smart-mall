package smart.lib;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;
import java.util.List;

public class Region {
    private final List<Region> children = new LinkedList<>();
    private final long code;
    private final String name;

    @JsonIgnore
    private final Region parent;

    public Region(Region parent, long code, String name) {
        this.parent = parent;
        this.code = code;
        this.name = name;
    }

    public long getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<Region> getChildren() {
        return children;
    }

    public Region getParent() {
        return parent;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(name);
        Region p = parent;
        while (p != null) {
            str.insert(0, p.name + " ");
            p = p.parent;
        }
        return str.toString();
    }
}
