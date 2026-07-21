package vn.thanhnd.demo.util.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Standard REST API response envelope used by all API controllers.
 *
 * @param <T> success payload type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"service_name", "success", "errors", "data"})
public class RestResponse<T> {

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("errors")
    private List<ErrorModel> errors;

    @JsonProperty("data")
    private T data;

    public RestResponse() {
    }

    public RestResponse(String serviceName, Boolean success, List<ErrorModel> errors, T data) {
        this.serviceName = serviceName;
        this.success = success;
        this.errors = errors;
        this.data = data;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<ErrorModel> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorModel> errors) {
        this.errors = errors;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
