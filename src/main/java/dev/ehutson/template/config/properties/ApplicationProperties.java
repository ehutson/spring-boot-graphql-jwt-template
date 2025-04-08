package dev.ehutson.template.config.properties;

import dev.ehutson.template.config.ApplicationDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public class ApplicationProperties {
    private final Mail mail = new Mail();
    private final Pagination pagination = new Pagination();

    public Mail getMail() {
        return mail;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public static class Mail {

        private boolean enabled = ApplicationDefaults.Mail.ENABLED;
        private String from = ApplicationDefaults.Mail.FROM;
        private String baseUrl = ApplicationDefaults.Mail.BASE_URL;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Pagination {
        private int pageSize = ApplicationDefaults.Pagination.DEFAULT_PAGE_SIZE;
        private int maxPageSize = ApplicationDefaults.Pagination.DEFAULT_MAX_PAGE_SIZE;

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getMaxPageSize() {
            return maxPageSize;
        }

        public void setMaxPageSize(int maxPageSize) {
            this.maxPageSize = maxPageSize;
        }
    }
}
