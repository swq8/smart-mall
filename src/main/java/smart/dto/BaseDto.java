package smart.dto;

import smart.util.Json;

abstract class BaseDto {

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
