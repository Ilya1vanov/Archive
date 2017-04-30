package server.spring.rest.protocol;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

/**
 * @author Ilya Ivanov
 */
public class HttpEntity implements Serializable {
    public static final HttpEntity EMPTY = new HttpEntity();
    private final HttpHeaders headers;
    private final String body;

    protected HttpEntity() {
        this((String)null, (MultiValueMap)null);
    }

    public HttpEntity(String body) {
        this(body, (MultiValueMap)null);
    }

    public HttpEntity(MultiValueMap<String, String> headers) {
        this((String)null, headers);
    }

    public HttpEntity(String body, MultiValueMap<String, String> headers) {
        this.body = body;
        HttpHeaders tempHeaders = new HttpHeaders();
        if(headers != null) {
            tempHeaders.putAll(headers);
        }

        this.headers = HttpHeaders.readOnlyHttpHeaders(tempHeaders);
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }

    public String getBody() {
        return this.body;
    }

    public boolean hasBody() {
        return this.body != null;
    }

    public boolean equals(Object other) {
        if(this == other) {
            return true;
        } else if(other != null && other.getClass() == this.getClass()) {
            HttpEntity otherEntity = (HttpEntity)other;
            return ObjectUtils.nullSafeEquals(this.headers, otherEntity.headers) && ObjectUtils.nullSafeEquals(this.body, otherEntity.body);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.headers) * 29 + ObjectUtils.nullSafeHashCode(this.body);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("<");
        if(this.body != null) {
            builder.append(this.body);
            if(this.headers != null) {
                builder.append(',');
            }
        }

        if(this.headers != null) {
            builder.append(this.headers);
        }

        builder.append('>');
        return builder.toString();
    }    
}
