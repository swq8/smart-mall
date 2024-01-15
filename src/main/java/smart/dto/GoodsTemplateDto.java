package smart.dto;

import jakarta.validation.constraints.NotNull;

public class GoodsTemplateDto {
    @NotNull
    String footer;
    @NotNull
    Boolean footerEnable;
    @NotNull
    String header;
    @NotNull
    Boolean headerEnable;

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public Boolean getFooterEnable() {
        return footerEnable;
    }

    public void setFooterEnable(Boolean footerEnable) {
        this.footerEnable = footerEnable;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Boolean getHeaderEnable() {
        return headerEnable;
    }

    public void setHeaderEnable(Boolean headerEnable) {
        this.headerEnable = headerEnable;
    }
}
