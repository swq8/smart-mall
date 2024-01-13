package smart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import smart.util.Json;
import smart.util.validategroups.Add;
import smart.util.validategroups.Edit;

import java.util.List;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "t_spec")
public class SpecEntity extends AbstractEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @NotNull(groups = Edit.class)
    @Null(groups = Add.class)
    private Long id;
    @NotBlank(groups = {Add.class, Edit.class})
    private String name;
    @NotNull(groups = {Add.class, Edit.class})
    private String note;
    @JsonIgnore
    @Column(columnDefinition = "text")
    private String items;

    @NotNull(groups = {Add.class, Edit.class})
    @Transient
    @Valid
    private List<Item> itemsObj;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public List<Item> getItemsObj() {
        if (itemsObj == null)
            itemsObj = Json.parseList(items, Item.class, true);
        return itemsObj;
    }


    public static class Item {
        @NotBlank(groups = {Add.class, Edit.class})
        String val;

        @NotNull(groups = {Add.class, Edit.class})
        String hint;
        @NotNull(groups = {Add.class, Edit.class})
        String img;

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }
    }

}