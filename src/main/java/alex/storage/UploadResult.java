package alex.storage;

public class UploadResult {
    private final String err;
    private final String url;

    public UploadResult(String err, String url) {
        this.err = err;
        this.url = url;
    }

    public String getErr() {
        return err;
    }

    public String getUrl() {
        return url;
    }
}
