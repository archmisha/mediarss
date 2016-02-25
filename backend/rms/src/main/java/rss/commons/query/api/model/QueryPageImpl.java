package rss.commons.query.api.model;

import static rss.commons.query.api.model.SyntaxConst.COMMA;
import static rss.commons.query.api.model.SyntaxConst.SPACE;

/**
 * @author shai.nagar@hp.com
 *         Date: 2/28/13
 */
class QueryPageImpl implements QueryPage {

    private int size = -1;
    private int offset = -1;

    /**
     * Constructs a new instance of QueryPageImpl.
     */
    QueryPageImpl() {
    }

    /**
     * Constructs a new instance of this class. This constructor is used by the generated ANTLR tree parser and includes
     * parser validation logic to ensure number expressions are integers.
     *
     * @param offset the page offset value.
     * @param size   the page size.
     */
    public QueryPageImpl(NumberExpression offset, NumberExpression size) {
        this.offset = Integer.parseInt(offset.getValue());
        this.size = Integer.parseInt(size.getValue());
    }

    /**
     * @return the page size assigned.
     */
    @Override
    public final int getSize() {
        return size;
    }

    /**
     * @param pageSize the page size to set.
     */
    @Override
    public final void setSize(int pageSize) {
        this.size = pageSize;
    }

    /**
     * @return the offset of the page.
     */
    @Override
    public final int getOffset() {
        return offset;
    }

    /**
     * @param pageOffset the offset to set.
     */
    @Override
    public final void setOffset(int pageOffset) {
        this.offset = pageOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toApiString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (offset > 0) {
            stringBuilder.append(offset);
        }
        stringBuilder.append(COMMA);
        stringBuilder.append(SPACE);
        if (size > 0) {
            stringBuilder.append(size);
        }
        return stringBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return toApiString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public final <T> void acceptVisitor(QueryPageVisitor<T> visitor, T context) {
        visitor.visit(this, context);
    }

}
