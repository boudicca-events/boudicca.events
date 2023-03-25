# EventResourcesApi

All URIs are relative to *http://localhost:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**eventGet**](EventResourcesApi.md#eventGet) | **GET** /event |  |
| [**eventPost**](EventResourcesApi.md#eventPost) | **POST** /event |  |


<a name="eventGet"></a>
# **eventGet**
> Set&lt;Event&gt; eventGet()



### Example
```java
// Import classes:
import at.cnoize.boudicca.invoker.ApiClient;
import at.cnoize.boudicca.invoker.ApiException;
import at.cnoize.boudicca.invoker.Configuration;
import at.cnoize.boudicca.invoker.models.*;
import at.cnoize.boudicca.api.EventResourcesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8081");

    EventResourcesApi apiInstance = new EventResourcesApi(defaultClient);
    try {
      Set<Event> result = apiInstance.eventGet();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling EventResourcesApi#eventGet");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Set&lt;Event&gt;**](Event.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

<a name="eventPost"></a>
# **eventPost**
> eventPost(event)



### Example
```java
// Import classes:
import at.cnoize.boudicca.invoker.ApiClient;
import at.cnoize.boudicca.invoker.ApiException;
import at.cnoize.boudicca.invoker.Configuration;
import at.cnoize.boudicca.invoker.models.*;
import at.cnoize.boudicca.api.EventResourcesApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8081");

    EventResourcesApi apiInstance = new EventResourcesApi(defaultClient);
    Event event = new Event(); // Event | 
    try {
      apiInstance.eventPost(event);
    } catch (ApiException e) {
      System.err.println("Exception when calling EventResourcesApi#eventPost");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **event** | [**Event**](Event.md)|  | [optional] |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Created |  -  |

