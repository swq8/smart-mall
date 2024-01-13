package smart.dto;

import jakarta.validation.constraints.NotNull;

/**
 * id data transfer object
 */
public class IdDto extends BaseDto {

    @NotNull
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
