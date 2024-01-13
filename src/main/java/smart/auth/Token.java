package smart.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import smart.entity.UserEntity;
import smart.util.Json;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Token {
    private String avatar;
    private long id;
    private int level;
    private String phone;
    private String name;


    public Token() {
    }

    public Token(UserEntity userEntity) {
        avatar = userEntity.getAvatar();
        id = userEntity.getId();
        level = userEntity.getLevel();
        phone = userEntity.getPhone();
        name = userEntity.getName();
    }

    public void assign(Token token) {
        token.avatar = avatar;
        token.id = id;
        token.phone = phone;
        token.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    /**
     * is value effective
     *
     * @return effective
     */
    @JsonIgnore
    public boolean isEffective() {
        return id > 0
                && level >= 0
                && name != null
                && !name.isEmpty();
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
