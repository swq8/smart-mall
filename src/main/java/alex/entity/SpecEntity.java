package alex.entity;

import javax.persistence.*;

@Entity
@Table(name = "spec")
public class SpecEntity {
    private long id;
    private String name;
    private String note;
    private long sort;
    private String list;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getSort() {
        return sort;
    }

    public void setSort(long sort) {
        this.sort = sort;
    }

    @Column(columnDefinition="text")
    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

}