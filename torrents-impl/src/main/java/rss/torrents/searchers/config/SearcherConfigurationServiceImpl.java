package rss.torrents.searchers.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.configuration.SettingsService;
import rss.torrents.searchers.SimpleTorrentSearcher;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: dikmanm
 * Date: 07/01/14 18:26
 */
@Service
public class SearcherConfigurationServiceImpl implements SearcherConfigurationService {

	private Map<String, SearcherConfiguration> searcherConfigurations = new ConcurrentHashMap<>();

	@Autowired
	private SearcherConfigurationDao searcherConfigurationDao;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	protected TransactionTemplate transactionTemplate;

    @Autowired
    private SettingsService settingsService;

	@PostConstruct
	@Transactional(propagation = Propagation.REQUIRED)
	public void postConstruct() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				reloadAllConfigurations();

				// verify all configurations exist, create missing
				List<String> searcherNames = new ArrayList<>();
				for (SimpleTorrentSearcher searcher : applicationContext.getBeansOfType(SimpleTorrentSearcher.class).values()) {
					searcherNames.add(searcher.getName());
				}

				boolean needReload = false;
				for (String searcherName : searcherNames) {
					if (!searcherConfigurations.containsKey(searcherName)) {
						createSearcherConfiguration(searcherName);
						needReload = true;
					}
				}

				if (needReload) {
					reloadAllConfigurations();
				}
			}
		});
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public SearcherConfiguration getSearcherConfiguration(String name) {
		if (!searcherConfigurations.containsKey(name)) {
			createSearcherConfiguration(name);
			// id updated, so better to query them all again to get the new id populated
			reloadAllConfigurations();
		}

		return searcherConfigurations.get(name);
	}

	@Override
	public Collection<SearcherConfiguration> getSearcherConfigurations() {
		return searcherConfigurations.values();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateSearcherConfiguration(SearcherConfiguration searcherConfiguration) {
		SearcherConfiguration updatedSearcherConfiguration = searcherConfigurationDao.merge(searcherConfiguration);
		searcherConfigurations.put(updatedSearcherConfiguration.getName(), updatedSearcherConfiguration);
	}

	@Override
	public void addDomain(String name, String domain) {
		SearcherConfiguration searcherConfiguration = searcherConfigurations.get(name);
		searcherConfiguration.getDomains().add(domain);
		updateSearcherConfiguration(searcherConfiguration);
	}

	@Override
	public void removeDomain(String name, String domain) {
		SearcherConfiguration searcherConfiguration = searcherConfigurations.get(name);
		searcherConfiguration.getDomains().remove(domain);
		updateSearcherConfiguration(searcherConfiguration);
	}

	private void reloadAllConfigurations() {
		searcherConfigurations.clear();
		for (SearcherConfiguration searcherConfiguration : searcherConfigurationDao.getAll()) {
			searcherConfigurations.put(searcherConfiguration.getName(), searcherConfiguration);
		}
	}

	private void createSearcherConfiguration(String name) {
		SearcherConfiguration searcherConfiguration = new SearcherConfiguration();
		searcherConfiguration.setName(name);
		searcherConfigurationDao.persist(searcherConfiguration);
	}

    @Override
    public void torrentzSetEnabled(boolean isEnabled) {
        settingsService.setPersistentSetting("torrentz.enabled", String.valueOf(isEnabled));
    }

    @Override
    public boolean torrentzIsEnabled() {
        return "true".equals(settingsService.getPersistentSetting("torrentz.enabled"));
    }
}
