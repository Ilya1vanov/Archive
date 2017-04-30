package server.spring.rest.protocol;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;

import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

public class ResponseEntity extends HttpEntity implements Serializable {
    private final HttpStatus statusCode;

    public ResponseEntity(HttpStatus status) {
        this((String)null, (MultiValueMap)null, status);
    }

    public ResponseEntity(String body, HttpStatus status) {
        this(body, (MultiValueMap)null, (HttpStatus)status);
    }

    public ResponseEntity(MultiValueMap<String, String> headers, HttpStatus status) {
        this((String)null, headers, (HttpStatus)status);
    }

    public ResponseEntity(String body, MultiValueMap<String, String> headers, HttpStatus status) {
        super(body, headers);
        Assert.notNull(status, "HttpStatus must not be null");
        this.statusCode = status;
    }

    public HttpStatus getStatusCode() {
        return this.statusCode;
    }

    public int getStatusCodeValue() {
        return this.statusCode.value();
    }

    public boolean equals(Object other) {
        if(this == other) {
            return true;
        } else if(!super.equals(other)) {
            return false;
        } else {
            ResponseEntity otherEntity = (ResponseEntity)other;
            return ObjectUtils.nullSafeEquals(this.statusCode, otherEntity.statusCode);
        }
    }

    public int hashCode() {
        return super.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.statusCode);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("<");
        builder.append(this.statusCode.toString());
        if(this.statusCode instanceof HttpStatus) {
            builder.append(' ');
            builder.append((this.statusCode).getReasonPhrase());
        }

        builder.append(',');
        String body = this.getBody();
        HttpHeaders headers = this.getHeaders();
        if(body != null) {
            builder.append(body);
            if(headers != null) {
                builder.append(',');
            }
        }

        if(headers != null) {
            builder.append(headers);
        }

        builder.append('>');
        return builder.toString();
    }

    public static ResponseEntity.BodyBuilder status(HttpStatus status) {
        Assert.notNull(status, "HttpStatus must not be null");
        return new ResponseEntity.DefaultBuilder(status);
    }

    public static ResponseEntity.BodyBuilder status(int status) {
        return new ResponseEntity.DefaultBuilder(HttpStatus.valueOf(status));
    }

    public static ResponseEntity.BodyBuilder ok() {
        return status(HttpStatus.OK);
    }

    public static  ResponseEntity ok(String body) {
        ResponseEntity.BodyBuilder builder = ok();
        return builder.body(body);
    }

    public static ResponseEntity.BodyBuilder created(URI location) {
        ResponseEntity.BodyBuilder builder = status(HttpStatus.CREATED);
        return (ResponseEntity.BodyBuilder)builder.location(location);
    }

    public static ResponseEntity.BodyBuilder accepted() {
        return status(HttpStatus.ACCEPTED);
    }

    public static ResponseEntity.HeadersBuilder noContent() {
        return status(HttpStatus.NO_CONTENT);
    }

    public static ResponseEntity.BodyBuilder badRequest() {
        return status(HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity.HeadersBuilder notFound() {
        return status(HttpStatus.NOT_FOUND);
    }

    public static ResponseEntity.BodyBuilder unprocessableEntity() {
        return status(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private static class DefaultBuilder implements ResponseEntity.BodyBuilder {
        private final HttpStatus statusCode;
        private final HttpHeaders headers = new HttpHeaders();

        public DefaultBuilder(HttpStatus statusCode) {
            this.statusCode = statusCode;
        }

        public ResponseEntity.BodyBuilder header(String headerName, String... headerValues) {
            String[] var3 = headerValues;
            int var4 = headerValues.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String headerValue = var3[var5];
                this.headers.add(headerName, headerValue);
            }

            return this;
        }

        public ResponseEntity.BodyBuilder headers(HttpHeaders headers) {
            if(headers != null) {
                this.headers.putAll(headers);
            }

            return this;
        }

        public ResponseEntity.BodyBuilder allow(HttpMethod... allowedMethods) {
            this.headers.setAllow(new LinkedHashSet(Arrays.asList(allowedMethods)));
            return this;
        }

        public ResponseEntity.BodyBuilder contentLength(long contentLength) {
            this.headers.setContentLength(contentLength);
            return this;
        }

        public ResponseEntity.BodyBuilder contentType(MediaType contentType) {
            this.headers.setContentType(contentType);
            return this;
        }

        public ResponseEntity.BodyBuilder eTag(String eag) {
            if(eag != null) {
                if(!eag.startsWith("\"") && !eag.startsWith("W/\"")) {
                    eag = "\"" + eag;
                }

                if(!eag.endsWith("\"")) {
                    eag = eag + "\"";
                }
            }

            this.headers.setETag(eag);
            return this;
        }

        public ResponseEntity.BodyBuilder lastModified(long date) {
            this.headers.setLastModified(date);
            return this;
        }

        public ResponseEntity.BodyBuilder location(URI location) {
            this.headers.setLocation(location);
            return this;
        }

        public ResponseEntity.BodyBuilder cacheControl(CacheControl cacheControl) {
            String ccValue = cacheControl.getHeaderValue();
            if(ccValue != null) {
                this.headers.setCacheControl(cacheControl.getHeaderValue());
            }

            return this;
        }

        public ResponseEntity.BodyBuilder varyBy(String... requestHeaders) {
            this.headers.setVary(Arrays.asList(requestHeaders));
            return this;
        }

        public ResponseEntity build() {
            return this.body((String)null);
        }

        public  ResponseEntity body(String body) {
            return new ResponseEntity(body, this.headers, this.statusCode);
        }
    }

    public interface BodyBuilder extends ResponseEntity.HeadersBuilder<ResponseEntity.BodyBuilder> {
        ResponseEntity.BodyBuilder contentLength(long var1);

        ResponseEntity.BodyBuilder contentType(MediaType var1);

        ResponseEntity body(String var1);
    }

    public interface HeadersBuilder<B extends ResponseEntity.HeadersBuilder<B>> {
        B header(String var1, String... var2);

        B headers(HttpHeaders var1);

        B allow(HttpMethod... var1);

        B eTag(String var1);

        B lastModified(long var1);

        B location(URI var1);

        B cacheControl(CacheControl var1);

        B varyBy(String... var1);

        ResponseEntity build();
    }
}
