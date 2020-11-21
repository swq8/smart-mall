package alex.authentication;

import alex.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Token {
    private String avatar;
    private long id;
    private int level;
    private String phone;
    private String name;
    private String password;
    private String salt;


    public Token() {
    }

    public Token(UserEntity userEntity) {
        avatar = userEntity.getAvatar();
        id = userEntity.getId();
        level = userEntity.getLevel();
        phone = userEntity.getPhone();
        name = userEntity.getName();
        password = userEntity.getPassword();
        salt = userEntity.getSalt();
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }


    /**
     * is value effective
     *
     * @return effective
     */
    public boolean isEffective() {
        return id > 0
                && level >= 0
                && name != null
                && name.length() > 0
                && password != null
                && password.length() > 0;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;

        try {
            json = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }
}
