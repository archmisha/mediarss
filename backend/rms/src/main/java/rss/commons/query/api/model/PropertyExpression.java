package rss.commons.query.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static rss.commons.query.api.model.SyntaxConst.DOT;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/27/13
 */
public class PropertyExpression implements Expression {

    private final List<String> segments = new ArrayList<>();

    /**
     * @param segment the segment to append to this property.
     */
    public final void addSegment(String segment) {
        segments.add(segment);
    }

    /**
     * @return a {@link java.util.List} of segments that composes the property name.
     */
    public final List<String> getSegments() {
        return segments;
    }

    /**
     * @param segments One or more segments.
     */
    public final void addSegments(List<String> segments) {
        this.segments.addAll(segments);
    }

    /**
     * @param segments One or more segments.
     */
    public final void addSegments(String... segments) {
        Collections.addAll(this.segments, segments);
    }

    @Override
    public final String toApiString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            if (i != 0) {
                stringBuilder.append(DOT);
            }
            stringBuilder.append(segments.get(i));
        }
        return stringBuilder.toString();
    }

    @Override
    public final String toString() {
        return toApiString();
    }

    @Override
    public final <T> void acceptVisitor(ExpressionVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }
}
