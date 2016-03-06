package rss.test.services;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import rss.test.Reporter;
import rss.test.util.HttpUtils;

import java.util.Map;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:43
 */
public abstract class BaseClient {

    @Autowired
    protected Reporter reporter;

    @Autowired
    protected HttpUtils httpUtils;

    protected String getContextPath() {
        return "";
    }

    protected abstract String getServiceName();

    protected String getBasePath() {
        String result = "";
        if (!StringUtils.isBlank(getContextPath())) {
            result += (getContextPath()) + "/";
        }
        result += ("rest/" + getServiceName());
        return result;
    }

    protected Map<String, Object> entityToMap(Object entity) {
        return new ObjectMapper().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
                .convertValue(entity, new TypeReference<Map<String, Object>>() {
                });
    }
}
