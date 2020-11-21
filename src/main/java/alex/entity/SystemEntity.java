package alex.entity;

import javax.persistence.*;

@Entity
@Table(name = "system")
public class SystemEntity {

    private long id;
    private String entity;
    private String attribute;
    private String value;


    @Id
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    @Column(columnDefinition = "text")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
