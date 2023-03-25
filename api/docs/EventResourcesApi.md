# EventResourcesApi

All URIs are relative to *http://localhost:8081*

Method | HTTP request | Description
------------- | ------------- | -------------
[**eventGet**](EventResourcesApi.md#eventGet) | **GET** /event | 
[**eventPost**](EventResourcesApi.md#eventPost) | **POST** /event | 


<a name="eventGet"></a>
# **eventGet**
> kotlin.collections.Set&lt;Event&gt; eventGet()



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import at.cnoize.boudicca.model.*

val apiInstance = EventResourcesApi()
try {
    val result : kotlin.collections.Set<Event> = apiInstance.eventGet()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling EventResourcesApi#eventGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling EventResourcesApi#eventGet")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.collections.Set&lt;Event&gt;**](Event.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="eventPost"></a>
# **eventPost**
> eventPost(event)



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import at.cnoize.boudicca.model.*

val apiInstance = EventResourcesApi()
val event : Event =  // Event | 
try {
    apiInstance.eventPost(event)
} catch (e: ClientException) {
    println("4xx response calling EventResourcesApi#eventPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling EventResourcesApi#eventPost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **event** | [**Event**](Event.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

