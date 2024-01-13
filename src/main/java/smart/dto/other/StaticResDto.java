package smart.dto.other;

import jakarta.validation.constraints.NotNull;

public class StaticResDto {
    @NotNull
    String path;
    @NotNull
    String version;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
