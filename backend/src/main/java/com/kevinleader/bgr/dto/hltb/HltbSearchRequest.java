package com.kevinleader.bgr.dto.hltb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HltbSearchRequest {

    @JsonProperty("searchType")
    private String searchType = "games";

    @JsonProperty("searchTerms")
    private List<String> searchTerms;

    @JsonProperty("searchPage")
    private int searchPage = 1;

    @JsonProperty("size")
    private int size = 5;

    @JsonProperty("searchOptions")
    private SearchOptions searchOptions = new SearchOptions();

    public HltbSearchRequest(List<String> searchTerms) {
        this.searchTerms = searchTerms;
    }

    public String getSearchType() { return searchType; }
    public List<String> getSearchTerms() { return searchTerms; }
    public int getSearchPage() { return searchPage; }
    public int getSize() { return size; }
    public SearchOptions getSearchOptions() { return searchOptions; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchOptions {

        @JsonProperty("games")
        private Games games = new Games();

        @JsonProperty("users")
        private Users users = new Users();

        @JsonProperty("filter")
        private String filter = "";

        @JsonProperty("sort")
        private int sort = 0;

        @JsonProperty("randomizer")
        private int randomizer = 0;

        public Games getGames() { return games; }
        public Users getUsers() { return users; }
        public String getFilter() { return filter; }
        public int getSort() { return sort; }
        public int getRandomizer() { return randomizer; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Games {

        @JsonProperty("userId")
        private int userId = 0;

        @JsonProperty("platform")
        private String platform = "";

        @JsonProperty("sortCategory")
        private String sortCategory = "popular";

        @JsonProperty("rangeCategory")
        private String rangeCategory = "main";

        @JsonProperty("rangeTime")
        private RangeTime rangeTime = new RangeTime();

        @JsonProperty("gameplay")
        private Gameplay gameplay = new Gameplay();

        @JsonProperty("rangeYear")
        private RangeYear rangeYear = new RangeYear();

        @JsonProperty("modifier")
        private String modifier = "";

        public int getUserId() { return userId; }
        public String getPlatform() { return platform; }
        public String getSortCategory() { return sortCategory; }
        public String getRangeCategory() { return rangeCategory; }
        public RangeTime getRangeTime() { return rangeTime; }
        public Gameplay getGameplay() { return gameplay; }
        public RangeYear getRangeYear() { return rangeYear; }
        public String getModifier() { return modifier; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RangeTime {

        @JsonProperty("min")
        private Integer min = null;

        @JsonProperty("max")
        private Integer max = null;

        public Integer getMin() { return min; }
        public Integer getMax() { return max; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Gameplay {

        @JsonProperty("perspective")
        private String perspective = "";

        @JsonProperty("flow")
        private String flow = "";

        @JsonProperty("genre")
        private String genre = "";

        @JsonProperty("difficulty")
        private String difficulty = "";

        public String getPerspective() { return perspective; }
        public String getFlow() { return flow; }
        public String getGenre() { return genre; }
        public String getDifficulty() { return difficulty; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RangeYear {

        @JsonProperty("min")
        private String min = "";

        @JsonProperty("max")
        private String max = "";

        public String getMin() { return min; }
        public String getMax() { return max; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Users {

        @JsonProperty("sortCategory")
        private String sortCategory = "postcount";

        public String getSortCategory() { return sortCategory; }
    }
}
