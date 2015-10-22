package rss.configuration.dao;

import rss.ems.entities.BaseEntity;

import javax.persistence.Column;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * User: dikmanm
 * Date: 26/02/13 23:30
 */
@javax.persistence.Entity(name = "Setting")
@javax.persistence.Table(name = "settings")
@NamedQueries({
        @NamedQuery(name = "Setting.findByKey",
                query = "select b from Setting as b where b.key = :key")
})
public class Setting extends BaseEntity {

    @Column(name = "key", unique = true)
    private String key;

    @Column(name = "value")
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
