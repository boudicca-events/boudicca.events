# ExampleResourceApi

All URIs are relative to *http://localhost:8081*

Method | HTTP request | Description
------------- | ------------- | -------------
[**helloGet**](ExampleResourceApi.md#helloGet) | **GET** /hello | 


<a name="helloGet"></a>
# **helloGet**
> kotlin.String helloGet()



### Example
```kotlin
// Import classes:
//import org.openapitools.client.infrastructure.*
//import at.cnoize.boudicca.model.*

val apiInstance = ExampleResourceApi()
try {
    val result : kotlin.String = apiInstance.helloGet()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ExampleResourceApi#helloGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ExampleResourceApi#helloGet")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**kotlin.String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

