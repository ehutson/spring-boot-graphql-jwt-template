package dev.ehutson.template.config;

public final class ApplicationDefaults {
    private ApplicationDefaults() {
    }

    public static class Pagination {
        public static final int DEFAULT_PAGE_SIZE = 10;
        public static final int DEFAULT_MAX_PAGE_SIZE = 100;
        private Pagination() {
        }
    }

    public static class Mail {
        public static final boolean ENABLED = false;
        public static final String FROM = "foo@bar.com";
        public static final String BASE_URL = "localhost:8080";
        private Mail() {
        }
    }
}
