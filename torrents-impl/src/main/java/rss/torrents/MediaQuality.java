package rss.torrents;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:27
 */
public enum MediaQuality {
    NORMAL {
        @Override
        public String toString() {
            return "";
        }
    },

    HD720P {
        @Override
        public String toString() {
            return "720p";
        }
    },

    HD1080P {
        @Override
        public String toString() {
            return "1080p";
        }
    };

    public static MediaQuality[] topToBottom() {
        return new MediaQuality[]{HD1080P, HD720P, NORMAL};
    }
}
