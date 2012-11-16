package org.entitypedia.games.common.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.entitypedia.games.common.model.ResultsPage;
import org.entitypedia.games.common.exceptions.ExceptionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class WordGamesClient {

    private static final Logger log = LoggerFactory.getLogger(WordGamesClient.class);

    protected final static String CHARSET = "UTF-8";
    protected static final TypeReference<Long> LONG_TYPE_REFERENCE = new TypeReference<Long>() {
    };
    protected static final TypeReference<Date> DATE_TYPE_REFERENCE = new TypeReference<Date>() {
    };
    protected static final TypeReference<Boolean> BOOLEAN_TYPE_REFERENCE = new TypeReference<Boolean>() {
    };

    protected String apiEndpoint = "http://localhost:9080/crosswords/webapi/";

    protected OAuthConsumer consumer;
    protected final ObjectMapper mapper = new ObjectMapper();

    public WordGamesClient(String uid, String password) {
        consumer = new DefaultOAuthConsumer(uid, password);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    protected String addPageSizeAndNo(String url, Integer pageSize, Integer pageNo) {
        StringBuilder listUrl = new StringBuilder(url);
        if (null != pageSize) {
            listUrl.append("pageSize=").append(Integer.toString(pageSize));
            if (null != pageNo) {
                listUrl.append("&");
            }
        }
        if (null != pageNo) {
            listUrl.append("pageNo=").append(Integer.toString(pageNo));
        }
        return listUrl.toString();
    }

    private <T> T doSimpleGet(String url, TypeReference<T> type) throws CrosswordException {
        log.debug("GETting url: " + url);
        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            try {
                connection.setRequestProperty("Accept-Charset", CHARSET);
                consumer.sign(connection);
                connection.connect();

                log.debug("Response code: " + connection.getResponseCode());
                if (200 == connection.getResponseCode()) {
                    InputStream in = null;
                    try {
                        if (log.isDebugEnabled()) {
                            final int BUFFER_SIZE = 1024 * 1024; // 1M buffer
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
                            in = new BufferedInputStream(connection.getInputStream());
                            int b;
                            while ((b = in.read()) != -1) {
                                bos.write(b);
                            }
                            byte[] buffer = bos.toByteArray();
                            String response = new String(buffer, "UTF-8");
                            log.debug("Response:\n" + response + "\n");
                            return mapper.readValue(buffer, type);
                        } else {
                            in = new BufferedInputStream(connection.getInputStream());
                            return mapper.readValue(in, type);
                        }
                    } finally {
                        if (null != in) {
                            in.close();
                        }
                    }
                } else {
                    throw processError(connection);
                }
            } finally {
                connection.disconnect();
            }
        } catch (MalformedURLException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthExpectationFailedException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthCommunicationException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthMessageSignerException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (IOException e) {
            throw new CrosswordException(e.getMessage(), e);
        }
    }

    private void doSimplePost(String url) throws CrosswordException {
        log.debug("POSTing url: " + url);
        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            try {
                connection.setRequestProperty("Accept-Charset", CHARSET);
                connection.setRequestMethod("POST");
                consumer.sign(connection);
                connection.connect();

                log.debug("Response code: " + connection.getResponseCode());
                if (200 == connection.getResponseCode()) {
                } else {
                    throw processError(connection);
                }
            } finally {
                connection.disconnect();
            }
        } catch (MalformedURLException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthExpectationFailedException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthCommunicationException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthMessageSignerException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (IOException e) {
            throw new CrosswordException(e.getMessage(), e);
        }
    }

    private void doPostObject(String url, Object object) throws CrosswordException {
        log.debug("POSTing object: " + url);
        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            try {
                connection.setRequestProperty("Accept-Charset", CHARSET);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(0);
                consumer.sign(connection);
                connection.connect();

                OutputStream out = null;
                try {
                    out = new BufferedOutputStream(connection.getOutputStream());
                    mapper.writeValue(out, object);
                } finally {
                    if (null != out) {
                        out.close();
                    }
                }

                log.debug("Response code: " + connection.getResponseCode());
                if (200 == connection.getResponseCode()) {
                } else {
                    throw processError(connection);
                }
            } finally {
                connection.disconnect();
            }
        } catch (MalformedURLException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthExpectationFailedException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthCommunicationException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthMessageSignerException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (IOException e) {
            throw new CrosswordException(e.getMessage(), e);
        }
    }

    private <T> T doPostReadObject(String url, Object object, TypeReference<T> type) throws CrosswordException {
        log.debug("POSTing object: " + url);
        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            try {
                connection.setRequestProperty("Accept-Charset", CHARSET);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(0);
                consumer.sign(connection);
                connection.connect();

                OutputStream out = null;
                try {
                    out = new BufferedOutputStream(connection.getOutputStream());
                    mapper.writeValue(out, object);
                } finally {
                    if (null != out) {
                        out.close();
                    }
                }

                log.debug("Response code: " + connection.getResponseCode());
                if (200 == connection.getResponseCode()) {
                    InputStream in = null;
                    try {
                        if (log.isDebugEnabled()) {
                            final int BUFFER_SIZE = 1024 * 1024; // 1M buffer
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
                            in = new BufferedInputStream(connection.getInputStream());
                            int b;
                            while ((b = in.read()) != -1) {
                                bos.write(b);
                            }
                            byte[] buffer = bos.toByteArray();
                            String response = new String(buffer, "UTF-8");
                            log.debug("Response:\n" + response + "\n");
                            return mapper.readValue(buffer, type);
                        } else {
                            in = new BufferedInputStream(connection.getInputStream());
                            return mapper.readValue(in, type);
                        }
                    } finally {
                        if (null != in) {
                            in.close();
                        }
                    }
                } else {
                    throw processError(connection);
                }
            } finally {
                connection.disconnect();
            }
        } catch (MalformedURLException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthExpectationFailedException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthCommunicationException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthMessageSignerException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (IOException e) {
            throw new CrosswordException(e.getMessage(), e);
        }
    }

    private <T> T doPostRead(String url, TypeReference<T> type) throws CrosswordException {
        log.debug("POSTing url: " + url);
        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            try {
                connection.setRequestProperty("Accept-Charset", CHARSET);
                connection.setRequestMethod("POST");
                consumer.sign(connection);
                connection.connect();

                log.debug("Response code: " + connection.getResponseCode());
                if (200 == connection.getResponseCode()) {
                    InputStream in = null;
                    try {
                        if (log.isDebugEnabled()) {
                            final int BUFFER_SIZE = 1024 * 1024; // 1M buffer
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
                            in = new BufferedInputStream(connection.getInputStream());
                            int b;
                            while ((b = in.read()) != -1) {
                                bos.write(b);
                            }
                            byte[] buffer = bos.toByteArray();
                            String response = new String(buffer, "UTF-8");
                            log.debug("Response:\n" + response + "\n");
                            return mapper.readValue(buffer, type);
                        } else {
                            in = new BufferedInputStream(connection.getInputStream());
                            return mapper.readValue(in, type);
                        }
                    } finally {
                        if (null != in) {
                            in.close();
                        }
                    }
                } else {
                    throw processError(connection);
                }
            } finally {
                connection.disconnect();
            }
        } catch (MalformedURLException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthExpectationFailedException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthCommunicationException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (OAuthMessageSignerException e) {
            throw new CrosswordException(e.getMessage(), e);
        } catch (IOException e) {
            throw new CrosswordException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private RuntimeException processError(HttpURLConnection connection) throws IOException {
        log.debug("Processing error...");
        InputStream err = null;
        try {
            if (log.isDebugEnabled()) {
                final int BUFFER_SIZE = 1024 * 1024; // 1M buffer
                ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
                err = new BufferedInputStream(connection.getErrorStream());
                int b;
                while ((b = err.read()) != -1) {
                    bos.write(b);
                }
                byte[] buffer = bos.toByteArray();
                String response = new String(buffer, "UTF-8");
                log.debug("Response:\n" + response + "\n");
                err = new ByteArrayInputStream(buffer);
            } else {
                err = new BufferedInputStream(connection.getErrorStream());
            }

            ExceptionDetails details = mapper.readValue(err, ExceptionDetails.class);
            if (null != details) {
                Class clazz = CrosswordException.class;
                try {
                    clazz = Class.forName(details.getExceptionClass());
                } catch (ClassNotFoundException e) {
                    log.debug("Exception class not found: " + details.getExceptionClass());
                }

                if (CrosswordException.class.isAssignableFrom(clazz)) {
                    Constructor<? extends CrosswordException> paramConstructor = null;
                    Constructor<? extends CrosswordException> stringConstructor = null;
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

                    CrosswordException ce = null;
                    try {
                        if (null != paramConstructor) {
                            Object arg = details.getParams();
                            ce = paramConstructor.newInstance(arg);
                        } else if (null != stringConstructor) {
                            ce = stringConstructor.newInstance(details.getErrorMessage());
                        }
                    } catch (InstantiationException e) {
                        throw new CrosswordException(e.getMessage(), e);
                    } catch (IllegalAccessException e) {
                        throw new CrosswordException(e.getMessage(), e);
                    } catch (InvocationTargetException e) {
                        throw new CrosswordException(e.getMessage(), e);
                    }
                    return ce;
                } else if (Throwable.class.isAssignableFrom(clazz)) {
                    Constructor<? extends Throwable> stringConstructor = null;
                    try {
                        stringConstructor = clazz.getConstructor(String.class);
                    } catch (NoSuchMethodException e) {
                        return new CrosswordException("Cannot find String constructor for exception: " + clazz.getName(), e);
                    }

                    Throwable t = null;
                    try {
                        t = stringConstructor.newInstance(details.getErrorMessage());
                    } catch (InstantiationException e) {
                        throw new CrosswordException(e.getMessage(), e);
                    } catch (IllegalAccessException e) {
                        throw new CrosswordException(e.getMessage(), e);
                    } catch (InvocationTargetException e) {
                        throw new CrosswordException(e.getMessage(), e);
                    }

                    if (RuntimeException.class.isAssignableFrom(clazz)) {
                        return (RuntimeException) t;
                    } else {
                        return new CrosswordException(details.getErrorMessage(), t);
                    }
                } else {
                    return new CrosswordException(details.getExceptionClass() + " is not assignable from Throwable");
                }
            } else {
                return new CrosswordException("Unable to parse error details");
            }
        } finally {
            if (null != err) {
                err.close();
            }
        }
    }
}