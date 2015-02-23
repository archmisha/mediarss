package rss.mail;

/**
 * User: dikmanm
 * Date: 20/02/2015 16:59
 */
public enum EmailClassification {
    NONE,
    JOB {
        @Override
        public String toString() {
            return "Jobs";
        }
    },
    NEW_USER {
        @Override
        public String toString() {
            return "Users";
        }
    },
    ERROR {
        @Override
        public String toString() {
            return "Errors";
        }
    },
    ANNOUNCEMENT {
        @Override
        public String toString() {
            return "Announcement";
        }
    },
    PASSWORD_RECOVERY {
        @Override
        public String toString() {
            return "Password Recovery";
        }
    },
    SUPPORT_FEATURE {
        @Override
        public String toString() {
            return "Feature";
        }
    },
    SUPPORT_DEFECT {
        @Override
        public String toString() {
            return "Defect";
        }
    }
}
