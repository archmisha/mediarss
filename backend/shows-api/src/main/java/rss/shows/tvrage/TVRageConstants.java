package rss.shows.tvrage;

/**
 * User: dikmanm
 * Date: 17/08/2015 14:44
 */
public class TVRageConstants {
    public class ShowListStatus {
        public static final int RETURNING_STATUS = 1;
        public static final int CANCELED_ENDED_STATUS = 2;
        public static final int TBD_STATUS = 3;
        public static final int IN_DEV_STATUS = 4;
        public static final int NEW_SERIES_STATUS = 7;
        public static final int ENDED2_STATUS = 11;
        public static final int CANCELED_STATUS = 13;
        public static final int ENDED_STATUS = 14;
    }

    public class ShowInfoStatus {
        public static final String ENDED_STATUS = "Ended";
        public static final String CANCELED_STATUS = "Canceled";
        public static final String CANCELED_OR_ENDED_STATUS = "Canceled/Ended";
        public static final String PILOT_REJECTED_STATUS = "Pilot Rejected";
        public static final String RETURNING_SERIES_STATUS = "Returning Series";
        public static final String IN_DEVELOPMENT_STATUS = "In Development";
        public static final String NEW_SERIES_STATUS = "New Series";
    }
}
