package rss.commons.query.api.model;

import java.util.List;

import static rss.commons.query.api.model.SyntaxConst.SP_COMMA;

/**
 * Internal utility class for model to-string implementations.
 *
 * @author shai.nagar@hp.com
 *         Date: 3/6/13
 */
class ApiStringUtils {

    static StringBuilder appendApiCommaSeparatedList(List<? extends QueryElement> elements, StringBuilder stringBuilder) {
        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                if (i != 0) {
                    stringBuilder.append(SP_COMMA);
                }
                stringBuilder.append(elements.get(i).toApiString());
            }
        }

        return stringBuilder;
    }

    private ApiStringUtils() {
    }
}
