package org.entitypedia.games.common.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.entitypedia.games.common.exceptions.ExceptionDetails;
import org.entitypedia.games.common.exceptions.WordGameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.Date;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class WordGameClient implements IWordGameClient {

    private static final Logger log = LoggerFactory.getLogger(WordGameClient.class);

    protected final static String CHARSET = "UTF-8";
    protected static final TypeReference<Integer> INTEGER_TYPE_REFERENCE = new TypeReference<Integer>() {
    };
    protected static final TypeReference<Long> LONG_TYPE_REFERENCE = new TypeReference<Long>() {
    };
    protected static final TypeReference<Date> DATE_TYPE_REFERENCE = new TypeReference<Date>() {
    };
    protected static final TypeReference<Boolean> BOOLEAN_TYPE_REFERENCE = new TypeReference<Boolean>() {
    };
    protected static final TypeReference<Double> DOUBLE_TYPE_REFERENCE = new TypeReference<Double>() {
    };

    protected String apiEndpoint = "http://localhost:9080/<game>/webapi/";

    protected ClientConnectionManager cm;
    protected HttpClient hc;

    protected final OAuthConsumer consumer;
    protected final ObjectMapper mapper = new ObjectMapper();

    protected boolean signConnection = true;

    public WordGameClient(String apiEndpoint, String uid, String password) {
        this.apiEndpoint = apiEndpoint;
        consumer = new CommonsHttpOAuthConsumer(uid, password);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.cm = new BasicClientConnectionManager();
        this.hc = new DefaultHttpClient(cm);
    }

    public WordGameClient(String apiEndpoint, String uid, String password, String token, String tokenSecret) {
        this.apiEndpoint = apiEndpoint;
        consumer = new CommonsHttpOAuthConsumer(uid, password);
        consumer.setTokenWithSecret(token, tokenSecret);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.cm = new BasicClientConnectionManager();
        this.hc = new DefaultHttpClient(cm);
    }

    @Override
    public String getApiEndpoint() {
        return apiEndpoint;
    }

    @Override
    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    public boolean getSignConnection() {
        return signConnection;
    }

    @Override
    public void setSignConnection(boolean signConnection) {
        this.signConnection = signConnection;
    }

    protected String encodeURL(String string) {
        if (null == string) {
            return null;
        } else {
            try {
                return URLEncoder.encode(string, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new WordGameException("UnsupportedEncodingException: " + e.getMessage(), e);
            }
        }
    }

    protected String addPageSizeAndNo(String url, Integer pageSize, Integer pageNo) {
        StringBuilder listUrl = new StringBuilder(url);
        if (null != pageSize) {
            if (-1 == listUrl.indexOf("?")) {
                listUrl.append('?');
            } else {
                listUrl.append('&');
            }
            listUrl.append("pageSize=").append(Integer.toString(pageSize));
        }
        if (null != pageNo) {
            if (-1 == listUrl.indexOf("?")) {
                listUrl.append('?');
            } else {
                listUrl.append('&');
            }
            listUrl.append("pageNo=").append(Integer.toString(pageNo));
        }
        return listUrl.toString();
    }

    protected String addPageSizeAndNoAndFilter(String url, Integer pageSize, Integer pageNo, String filter) {
        StringBuilder listUrl = new StringBuilder(url);
        if (null != pageSize) {
            if (-1 == listUrl.indexOf("?")) {
                listUrl.append('?');
            } else {
                listUrl.append('&');
            }
            listUrl.append("pageSize=").append(Integer.toString(pageSize));
        }
        if (null != pageNo) {
            if (-1 == listUrl.indexOf("?")) {
                listUrl.append('?');
            } else {
                listUrl.append('&');
            }
            listUrl.append("pageNo=").append(Integer.toString(pageNo));
        }
        if (null != filter) {
            if (-1 == listUrl.indexOf("?")) {
                listUrl.append('?');
            } else {
                listUrl.append('&');
            }
            listUrl.append("filter=").append(filter);
        }
        return listUrl.toString();
    }

    protected String addPageSizeAndNoAndFilterAndOrder(String url, Integer pageSize, Integer pageNo, String filter, String order) {
        StringBuilder listUrl = new StringBuilder(url);
        if (null != pageSize) {
            if (-1 == listUrl.indexOf("?")) {
                listUrl.append('?');
            } else {
                listUrl.append('&');
            }
            listUrl.append("pageSize=").append(Integer.toString(pageSize));
        }
        if (null != pageNo) {
            if (-1 == listUrl.indexOf("?")) {
                listUrl.append('?');
            } else {
                listUrl.append('&');
            }
            listUrl.append("pageNo=").append(Integer.toString(pageNo));
        }
        if (null != filter) {
            if (-1 == listUrl.indexOf("?")) {
                listUrl.append('?');
            } else {
                listUrl.append('&');
            }
            listUrl.append("filter=").append(filter);
        }
        if (null != order) {
            if (-1 == listUrl.indexOf("?")) {
                listUrl.append('?');
            } else {
                listUrl.append('&');
            }
            listUrl.append("order=").append(order);
        }
        return listUrl.toString();
    }

    protected void doEmptyGet(String url) throws WordGameException {
        log.debug("GETting url: " + url);
        try {
            HttpGet request = new HttpGet(url);
            try {
                request.addHeader("Accept-Charset", CHARSET);

                if (signConnection) {
                    consumer.sign(request);
                }

                HttpResponse response = hc.execute(request);
                try {
                    log.debug("Response code: " + response.getStatusLine().getStatusCode());
                    if (200 != response.getStatusLine().getStatusCode()) {
                        throw processError(response);
                    }
                } finally {
                    HttpClientUtils.closeQuietly(response);
                }
            } finally {
                request.releaseConnection();
            }
        } catch (OAuthExpectationFailedException | OAuthCommunicationException | OAuthMessageSignerException | IOException e) {
            throw new WordGameException(e.getMessage(), e);
        }
    }


    protected <T> T doSimpleGet(String url, TypeReference<T> type) throws WordGameException {
        log.debug("GETting url: " + url);
        try {
            HttpGet request = new HttpGet(url);
            try {
                request.addHeader("Accept-Charset", CHARSET);

                if (signConnection) {
                    consumer.sign(request);
                }
                HttpResponse response = hc.execute(request);

                log.debug("Response code: " + response.getStatusLine().getStatusCode());
                if (200 == response.getStatusLine().getStatusCode()) {
                    InputStream in = null;
                    try {
                        if (log.isDebugEnabled()) {
                            final int BUFFER_SIZE = 1024 * 1024; // 1M buffer
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
                            in = new BufferedInputStream(response.getEntity().getContent());
                            int b;
                            while ((b = in.read()) != -1) {
                                bos.write(b);
                            }
                            byte[] buffer = bos.toByteArray();
                            String content = new String(buffer, "UTF-8");
                            log.debug("Response:\n" + content + "\n");
                            return mapper.readValue(buffer, type);
                        } else {
                            in = new BufferedInputStream(response.getEntity().getContent());
                            return mapper.readValue(in, type);
                        }
                    } finally {
                        if (null != in) {
                            in.close();
                        }
                        HttpClientUtils.closeQuietly(response);
                    }
                } else {
                    throw processError(response);
                }
            } finally {
                request.releaseConnection();
            }
        } catch (OAuthExpectationFailedException | OAuthCommunicationException | OAuthMessageSignerException | IOException e) {
            throw new WordGameException(e.getMessage(), e);
        }
    }

    protected void doSimplePost(String url) throws WordGameException {
        log.debug("POSTing url: " + url);
        try {
            HttpPost request = new HttpPost(url);
            try {
                request.addHeader("Accept-Charset", CHARSET);
                if (signConnection) {
                    consumer.sign(request);
                }
                HttpResponse response = hc.execute(request);
                try {
                    log.debug("Response code: " + response.getStatusLine().getStatusCode());
                    if (200 != response.getStatusLine().getStatusCode()) {
                        throw processError(response);
                    }
                } finally {
                    HttpClientUtils.closeQuietly(response);
                }
            } finally {
                request.releaseConnection();
            }
        } catch (OAuthExpectationFailedException | OAuthCommunicationException | OAuthMessageSignerException | IOException e) {
            throw new WordGameException(e.getMessage(), e);
        }
    }

    protected void doPostObject(String url, Object object) throws WordGameException {
        log.debug("POSTing object: " + url);
        try {
            HttpPost request = new HttpPost(url);
            try {
                request.addHeader("Accept-Charset", CHARSET);
                request.addHeader("Content-Type", "application/json");

                request.setEntity(new ByteArrayEntity(mapper.writeValueAsBytes(object)));

                if (signConnection) {
                    consumer.sign(request);
                }

                HttpResponse response = hc.execute(request);
                try {
                    log.debug("Response code: " + response.getStatusLine().getStatusCode());
                    if (200 != response.getStatusLine().getStatusCode()) {
                        throw processError(response);
                    }
                } finally {
                    HttpClientUtils.closeQuietly(response);
                }
            } finally {
                request.releaseConnection();
            }
        } catch (OAuthExpectationFailedException | OAuthCommunicationException | OAuthMessageSignerException | IOException e) {
            throw new WordGameException(e.getMessage(), e);
        }
    }

    protected <T> T doPostReadObject(String url, Object object, TypeReference<T> type) throws WordGameException {
        log.debug("POSTing object: " + url);
        try {
            HttpPost request = new HttpPost(url);
            try {
                request.addHeader("Accept-Charset", CHARSET);
                request.addHeader("Content-Type", "application/json");

                request.setEntity(new ByteArrayEntity(mapper.writeValueAsBytes(object)));

                if (signConnection) {
                    consumer.sign(request);
                }

                HttpResponse response = hc.execute(request);
                if (200 == response.getStatusLine().getStatusCode()) {
                    InputStream in = null;
                    try {
                        if (log.isDebugEnabled()) {
                            final int BUFFER_SIZE = 1024 * 1024; // 1M buffer
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
                            in = new BufferedInputStream(response.getEntity().getContent());
                            int b;
                            while ((b = in.read()) != -1) {
                                bos.write(b);
                            }
                            byte[] buffer = bos.toByteArray();
                            String content = new String(buffer, "UTF-8");
                            log.debug("Response:\n" + content + "\n");
                            return mapper.readValue(buffer, type);
                        } else {
                            in = new BufferedInputStream(response.getEntity().getContent());
                            return mapper.readValue(in, type);
                        }
                    } finally {
                        if (null != in) {
                            in.close();
                        }
                        HttpClientUtils.closeQuietly(response);
                    }
                } else {
                    throw processError(response);
                }
            } finally {
                request.releaseConnection();
            }
        } catch (OAuthExpectationFailedException | OAuthCommunicationException | OAuthMessageSignerException | IOException e) {
            throw new WordGameException(e.getMessage(), e);
        }
    }

    protected <T> T doPostRead(String url, TypeReference<T> type) throws WordGameException {
        log.debug("POSTing url: " + url);
        try {
            HttpPost request = new HttpPost(url);
            try {
                request.addHeader("Accept-Charset", CHARSET);
                request.addHeader("Content-Type", "application/json");

                if (signConnection) {
                    consumer.sign(request);
                }
                HttpResponse response = hc.execute(request);
                if (200 == response.getStatusLine().getStatusCode()) {
                    InputStream in = null;
                    try {
                        if (log.isDebugEnabled()) {
                            final int BUFFER_SIZE = 1024 * 1024; // 1M buffer
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
                            in = new BufferedInputStream(response.getEntity().getContent());
                            int b;
                            while ((b = in.read()) != -1) {
                                bos.write(b);
                            }
                            byte[] buffer = bos.toByteArray();
                            String content = new String(buffer, "UTF-8");
                            log.debug("Response:\n" + content + "\n");
                            return mapper.readValue(buffer, type);
                        } else {
                            in = new BufferedInputStream(response.getEntity().getContent());
                            return mapper.readValue(in, type);
                        }
                    } finally {
                        if (null != in) {
                            in.close();
                        }
                        HttpClientUtils.closeQuietly(response);
                    }
                } else {
                    throw processError(response);
                }
            } finally {
                request.releaseConnection();
            }
        } catch (OAuthExpectationFailedException | OAuthCommunicationException | OAuthMessageSignerException | IOException e) {
            throw new WordGameException(e.getMessage(), e);
        }
    }

    protected RuntimeException processError(HttpResponse response) throws IOException {
        return processError(response.getEntity().getContent(), mapper);
    }

    @SuppressWarnings("unchecked")
    public static RuntimeException processError(InputStream errorStream, ObjectMapper mapper) throws IOException {
        log.debug("Processing error...");
        InputStream err = null;
        try {
            if (log.isDebugEnabled()) {
                final int BUFFER_SIZE = 1024 * 1024; // 1M buffer
                ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
                err = new BufferedInputStream(errorStream);
                int b;
                while ((b = err.read()) != -1) {
                    bos.write(b);
                }
                byte[] buffer = bos.toByteArray();
                String response = new String(buffer, "UTF-8");
                log.debug("Response:\n" + response + "\n");
                err = new ByteArrayInputStream(buffer);
            } else {
                err = new BufferedInputStream(errorStream);
            }

            ExceptionDetails details = mapper.readValue(err, ExceptionDetails.class);
            if (null != details) {
                Class clazz = WordGameException.class;
                try {
                    clazz = Class.forName(details.getExceptionClass());
                } catch (ClassNotFoundException e) {
                    log.debug("Exception class not found: " + details.getExceptionClass());
                }

                if (WordGameException.class.isAssignableFrom(clazz)) {
                    Constructor<? extends WordGameException> paramConstructor = null;
                    Constructor<? extends WordGameException> stringConstructor = null;
                    try {
                        paramConstructor = clazz.getConstructor(Object[].class);
                    } catch (NoSuchMethodException e) {
                        log.debug("Cannot find paramConstructor for class: " + clazz.getName());
                    }
                    if (null == paramConstructor) {
                        try {
                            stringConstructor = clazz.getConstructor(String.class);
                        } catch (NoSuchMethodException e) {
                            log.debug("Cannot find stringConstructor for class: " + clazz.getName());
                        }
                    }

                    WordGameException ce = null;
                    try {
                        if (null != paramConstructor) {
                            Object arg = details.getParams();
                            ce = paramConstructor.newInstance(arg);
                        } else if (null != stringConstructor) {
                            ce = stringConstructor.newInstance(details.getErrorMessage());
                        }
                    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                        throw new WordGameException(e.getMessage(), e);
                    }
                    return ce;
                } else if (Throwable.class.isAssignableFrom(clazz)) {
                    Constructor<? extends Throwable> stringConstructor;
                    try {
                        stringConstructor = clazz.getConstructor(String.class);
                    } catch (NoSuchMethodException e) {
                        return new WordGameException("Cannot find String constructor for exception: " + clazz.getName(), e);
                    }

                    Throwable t;
                    try {
                        t = stringConstructor.newInstance(details.getErrorMessage());
                    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                        throw new WordGameException(e.getMessage(), e);
                    }

                    if (RuntimeException.class.isAssignableFrom(clazz)) {
                        return (RuntimeException) t;
                    } else {
                        return new WordGameException(details.getErrorMessage(), t);
                    }
                } else {
                    return new WordGameException(details.getExceptionClass() + " is not assignable from Throwable");
                }
            } else {
                return new WordGameException("Unable to parse error details");
            }
        } finally {
            if (null != err) {
                err.close();
            }
        }
    }

    public ClientConnectionManager getConnectionManager() {
        return cm;
    }

    public void setConnectionManager(ClientConnectionManager cm) {
        this.cm = cm;
        this.hc = new DefaultHttpClient(cm);
    }

    public HttpClient getHttpClient() {
        return hc;
    }

    public void setHttpClient(HttpClient hc) {
        this.hc = hc;
    }
}