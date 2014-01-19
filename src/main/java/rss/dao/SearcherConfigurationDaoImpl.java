package rss.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import rss.entities.SearcherConfiguration;
import rss.entities.SearcherConfigurationEntity;

import java.util.*;

/**
 * User: dikmanm
 * Date: 07/01/14 18:49
 */
@Repository
public class SearcherConfigurationDaoImpl extends BaseDaoJPA<SearcherConfigurationEntity> implements SearcherConfigurationDao {

	@Override
	public SearcherConfiguration findByName(String name) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("name", name.toLowerCase());
		SearcherConfigurationEntity searcherConfigurationEntity = uniqueResult(super.<SearcherConfigurationEntity>findByNamedQueryAndNamedParams("SearcherConfigurationEntity.findByName", params));
		return toSearcherConfiguration(searcherConfigurationEntity);
	}

	@Override
	public Collection<SearcherConfiguration> getAll() {
		Collection<SearcherConfiguration> result = new ArrayList<>();
		for (SearcherConfigurationEntity searcherConfigurationEntity : super.findAll()) {
			result.add(toSearcherConfiguration(searcherConfigurationEntity));
		}
		return result;
	}

	@Override
	public void persist(SearcherConfiguration searcherConfiguration) {
		super.persist(toSearcherConfigurationEntity(searcherConfiguration));
	}

	@Override
	public SearcherConfiguration merge(SearcherConfiguration searcherConfiguration) {
		return toSearcherConfiguration(super.merge(toSearcherConfigurationEntity(searcherConfiguration)));
	}

	private SearcherConfigurationEntity toSearcherConfigurationEntity(SearcherConfiguration searcherConfiguration) {
		SearcherConfigurationEntity searcherConfigurationEntity = new SearcherConfigurationEntity();
		searcherConfigurationEntity.setId(searcherConfiguration.getId());
		searcherConfigurationEntity.setName(searcherConfiguration.getName());
		searcherConfigurationEntity.setDns(StringUtils.join(searcherConfiguration.getDomains(), ","));
		return searcherConfigurationEntity;
	}

	private SearcherConfiguration toSearcherConfiguration(SearcherConfigurationEntity searcherConfigurationEntity) {
		SearcherConfiguration searcherConfiguration = new SearcherConfiguration();
		searcherConfiguration.setId(searcherConfigurationEntity.getId());
		searcherConfiguration.setName(searcherConfigurationEntity.getName());
		searcherConfiguration.getDomains().addAll(Arrays.asList(StringUtils.split(searcherConfigurationEntity.getDns(), ",")));
		return searcherConfiguration;
	}
}
