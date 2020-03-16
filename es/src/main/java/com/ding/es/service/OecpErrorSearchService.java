package com.ding.es.service;

import com.ding.es.document.OecpError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ding.es.util.Constant.INDEX;

@Service
@Slf4j
public class OecpErrorSearchService {
    private RestHighLevelClient client;
    private ObjectMapper objectMapper;

    @Autowired
    public OecpErrorSearchService(RestHighLevelClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public String createErrorCodeIndex() throws IOException {
        ClassPathResource resource = new ClassPathResource("mapping.json");
        String mappings = IOUtils.toString(resource.getInputStream(), "UTF-8");
        resource.getInputStream().close();
        CreateIndexRequest request = new CreateIndexRequest(INDEX);
        request.mapping(mappings, XContentType.JSON);
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        boolean acknowledged = response.isAcknowledged();
        System.out.println("创建索引的结果：" + acknowledged);
        if (acknowledged) {
            return "create success";
        } else {
            return "create fail";
        }
    }

    public OecpError findById(String id) throws Exception {
        GetRequest getRequest = new GetRequest(INDEX, id);
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> resultMap = getResponse.getSource();
        return convertMapToProfileDocument(resultMap);
    }

    /*public List<OecpError> findById(String id) throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQuery = QueryBuilders
                .termQuery("id", id);
        searchSourceBuilder.query(termQuery);
        SearchResponse searchResponse =
                client.search(searchRequest, RequestOptions.DEFAULT);
        return getSearchResult(searchResponse);
    }*/

    public String createOrUpdateErrorCode(OecpError document) throws Exception {
        IndexRequest indexRequest = new IndexRequest(INDEX);
        indexRequest.id(document.getId());
        // 如果属性和 mapping 映射文件不一致，会覆盖
        indexRequest.source(convertProfileDocumentToMap(document));
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        DocWriteResponse.Result result = indexResponse.getResult();
        System.out.println("更新索引的结果：" + result);
        return result.toString();
    }

    /*public String updateProfile(OecpError document) throws Exception {
        OecpError errorCode = findById(document.getId());
        UpdateRequest updateRequest = new UpdateRequest(INDEX, errorCode.getId());
        updateRequest.doc(convertProfileDocumentToMap(document));
        UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        GetResult getResult = updateResponse.getGetResult();
        System.out.println("更新后的结果 ：" + getResult);
        return "更新成功";
        *//*OecpError resultDocument = findById(document.getId());
        UpdateRequest updateRequest = new UpdateRequest(
                INDEX,
                TYPE,
                resultDocument.getId());
        updateRequest.doc(convertProfileDocumentToMap(document));
        UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        return updateResponse
                .getResult()
                .name();*//*
    }*/

    public List<OecpError> findAll() throws Exception {
        SearchRequest searchRequest = buildSearchRequest(INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse =
                client.search(searchRequest, RequestOptions.DEFAULT);
        return getSearchResult(searchResponse);
    }

    public List<OecpError> suggestErrorCode(String info) throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        CompletionSuggestionBuilder completionSuggestionBuilder = SuggestBuilders
                .completionSuggestion("errorMsg.msgSuggest")
                .prefix(info);
        suggestBuilder.addSuggestion("msgSuggest", completionSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse =
                client.search(searchRequest, RequestOptions.DEFAULT);
        return getSuggestResult(searchResponse);
    }

    public List<OecpError> searchErrorCode(String info) throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("id", info))
                .should(QueryBuilders.matchQuery("errorCode", info))
                .should(QueryBuilders.matchQuery("errorMsg", info))
                .should(QueryBuilders.matchQuery("errorDesc", info))
                .should(QueryBuilders.matchQuery("errorTag", info));
        searchSourceBuilder.query(boolQuery);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse =
                client.search(searchRequest, RequestOptions.DEFAULT);
        return getSearchResult(searchResponse);
    }

    public String deleteErrorCode(String id) throws Exception {
        DeleteRequest deleteRequest = new DeleteRequest(INDEX, id);
        DeleteResponse response = client.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println("删除结果：" + response.getResult());
        return response
                .getResult()
                .name();
    }


    private Map<String, Object> convertProfileDocumentToMap(OecpError profileDocument) {
        return objectMapper.convertValue(profileDocument, Map.class);
       /* HashMap<String, Object> map = new HashMap<>();
        map.put("id", profileDocument.getId());
        map.put("error_code", profileDocument.getErrorCode());
        map.put("error_msg", profileDocument.getErrorMsg());
        map.put("error_desc", profileDocument.getErrorDesc());
        map.put("error_tag", profileDocument.getErrorTag());
        return map;*/
    }

    private Map<String, Object> convertErrorCodeToMap(OecpError profileDocument) {
        return objectMapper.convertValue(profileDocument, Map.class);
    }

    private OecpError convertMapToProfileDocument(Map<String, Object> map) {
        return objectMapper.convertValue(map, OecpError.class);
    }

    private List<OecpError> getSuggestResult(SearchResponse response) {
        Suggest suggest = response.getSuggest();
        CompletionSuggestion completionSuggestion = suggest.getSuggestion("msgSuggest");
        List<OecpError> errorCodeDocuments = new ArrayList<>();
        for (CompletionSuggestion.Entry entry : completionSuggestion.getEntries()) {
            for (CompletionSuggestion.Entry.Option option : entry) {
                SearchHit hit = option.getHit();
                errorCodeDocuments
                        .add(objectMapper
                                .convertValue(hit.getSourceAsMap(), OecpError.class));
            }
        }
        return errorCodeDocuments;

    }

    private List<OecpError> getSearchResult(SearchResponse response) {
        SearchHit[] searchHit = response.getHits().getHits();
        List<OecpError> errorCodeDocuments = new ArrayList<>();
        for (SearchHit hit : searchHit) {
            errorCodeDocuments
                    .add(objectMapper
                            .convertValue(hit
                                    .getSourceAsMap(), OecpError.class));
        }
        return errorCodeDocuments;
    }

    private SearchRequest buildSearchRequest(String index) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        //searchRequest.types(type);
        return searchRequest;
    }


}
