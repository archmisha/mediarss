package rss;

import org.springframework.stereotype.Component;
import rss.shows.tvrage.TVRageConstants;
import rss.shows.tvrage.TVRageShow;
import rss.shows.tvrage.TVRageShowInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 17/08/2015 10:05
 */
@Component
public class TestPagesServiceImpl {

    private List<TVRageShow> showList = new ArrayList<>();
    private Map<Integer, TVRageShowInfo> showsInfo = new HashMap<>();

//    @PostConstruct
//    public void postConstruct() {
//        loadShowsList();
//    }

    public void resetOverrides() {
        showList = new ArrayList<>();
//        loadShowsList();
    }

    public void markShowEnded(long showId) {
        for (TVRageShow show : showList) {
            if (show.getId() == showId) {
                show.setStatus(TVRageConstants.ShowListStatus.ENDED_STATUS);
                return;
            }
        }

        throw new RuntimeException("Show " + showId + " not found");
    }

//    private void loadShowsList() {
//        try {
//            String str = IOUtils.toString(new ClassPathResource("showsList.json", this.getClass().getClassLoader()).getInputStream());
//            showList = Lists.newArrayList(JsonTranslation.jsonString2Object(str, TVRageShow[].class));
//        } catch (Exception e) {
//            throw new RuntimeException(e.getMessage(), e);
//        }
//    }

    public List<TVRageShow> getShowsList() {
        return showList;
    }

    public void createShow(TVRageShow show) {
        showList.add(show);
    }

    public void createShowInfo(TVRageShowInfo showInfo) {
        showsInfo.put(showInfo.getShowid(), showInfo);
    }

    public TVRageShowInfo getShowInfo(int showId) {
        return showsInfo.get(showId);
    }
}
