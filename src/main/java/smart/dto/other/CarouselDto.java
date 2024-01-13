package smart.dto.other;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CarouselDto {
    @NotEmpty
    @Valid
    List<CarouselItem> items;

    public List<CarouselItem> getItems() {
        return items;
    }

    public void setItems(List<CarouselItem> items) {
        this.items = items;
    }
}
