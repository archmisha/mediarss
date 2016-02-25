package rss.commons.query.api.model;

/**
 * Created with IntelliJ IDEA.
 * User: sbruce
 * Date: 06/08/2013
 * Time: 16:21
 */
public class AnalyticsFrameElement implements QueryElement {

    public enum RangeRows implements QueryElement {
        RANGE("RANGE"),
        ROWS("ROWS");

        private final String dqlString;

        private RangeRows(final String dqlString) {
            this.dqlString = dqlString;
        }

        @Override
        public String toApiString() {
            return dqlString;
        }

        @Override
        public String toString() {
            return toApiString();
        }
    }

    public enum Frame implements QueryElement {
        UNBOUNDED_PRECEDING("UNBOUNDED PRECEDING"),
        PRECEDING("PRECEDING"),
        CURRENT_ROW("CURRENT ROW"),
        FOLLOWING("FOLLOWING"),
        UNBOUNDED_FOLLOWING("UNBOUNDED FOLLOWING");

        private final String dqlString;

        private Frame(final String dqlString) {
            this.dqlString = dqlString;
        }

        @Override
        public String toApiString() {
            return dqlString;
        }

        @Override
        public String toString() {
            return toApiString();
        }
    }

    private RangeRows rangeRows;
    private Frame frameStart;
    private String frameStartValue;
    private Frame frameEnd;
    private String frameEndValue;

    public AnalyticsFrameElement() {
        // intentionally left blank
    }

    public AnalyticsFrameElement(final RangeRows rangeRows) {
        this.rangeRows = rangeRows;
    }

    public RangeRows getRangeRows() {
        return rangeRows;
    }

    public void setRangeRows(RangeRows rangeRows) {
        this.rangeRows = rangeRows;
    }

    public Frame getFrameStart() {
        return frameStart;
    }

    public void setFrameStart(Frame frameStart) {
        this.frameStart = frameStart;
    }

    public String getFrameStartValue() {
        return frameStartValue;
    }

    public void setFrameStartValue(String frameStartValue) {
        this.frameStartValue = frameStartValue;
    }

    public Frame getFrameEnd() {
        return frameEnd;
    }

    public void setFrameEnd(Frame frameEnd) {
        this.frameEnd = frameEnd;
    }

    public String getFrameEndValue() {
        return frameEndValue;
    }

    public void setFrameEndValue(String frameEndValue) {
        this.frameEndValue = frameEndValue;
    }

    @Override
    public String toApiString() {
        final StringBuilder message = new StringBuilder();
        message.append(rangeRows);
        message.append(SyntaxConst.SPACE);

        if (frameEnd != null) {
            message.append(SyntaxConst.KW_BETWEEN);
            message.append(SyntaxConst.SPACE);
        }

        if (frameStartValue != null) {
            message.append(frameStartValue);
            message.append(SyntaxConst.SPACE);
        }

        message.append(frameStart);

        if (frameEnd != null) {
            message.append(SyntaxConst.SPACE);
            message.append(SyntaxConst.KW_AND);
            message.append(SyntaxConst.SPACE);
            if (frameEndValue != null) {
                message.append(frameEndValue);
                message.append(SyntaxConst.SPACE);
            }
            message.append(frameEnd);
        }

        return message.toString();
    }

    @Override
    public String toString() {
        return toApiString();
    }
}
