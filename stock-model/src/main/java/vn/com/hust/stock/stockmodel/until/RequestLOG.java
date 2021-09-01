package vn.com.hust.stock.stockmodel.until;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;
import vn.com.hust.stock.stockmodel.login.ErrorResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class RequestLOG {
    private static final String HTTP_STATUS = "httpStatus";
    private static final String URL = "url";
    private static final String METHOD = "method";
    private static final String BODY = "body";
    private static final String RESPONSE = "response";
    private static final String USER = "user";

    private static final Logger logger = LoggerFactory.getLogger(RequestLOG.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void error(HttpServletRequest request, int httpStatus, ErrorResponse errResp, Throwable ex) {
        JsonNode msg = createLogMsg(request, httpStatus, errResp);
        if (httpStatus < 400) {
            logger.info("{}", msg.toString(), ex);
        } else if (httpStatus < 500) {
            logger.warn("{}", msg.toString(), ex);
        } else {
            logger.error("{}", msg.toString(), ex);
        }
    }

    public static void info(HttpServletRequest request, HttpServletResponse response) {
        if (response.getStatus() < 300) {
            JsonNode msg = createLogMsg(request, response.getStatus());
            logger.info("{}", msg.toString());
        }
    }

    private static ObjectNode createLogMsg(HttpServletRequest request, int httpStatus) {
        ObjectNode objectMsg = objectMapper.createObjectNode()
                .put(HTTP_STATUS, httpStatus);
        String url = request.getRequestURL().toString();
        if (request.getQueryString() != null) {
            url = url + "?" + request.getQueryString();
        }
        objectMsg.put(URL, url)
                .put(METHOD, request.getMethod());
//        if (auth instanceof UserAuthentication) {
//            UserDetails user = (UserDetails) auth.getDetails();
//            objectMsg.put(USER, user.getUsername());
//        }
        try {
            String body = getRequestBody(request);
            if (body != null && request.getContentType() != null && request.getContentType().toLowerCase().startsWith("application/json")) {
                objectMsg.set(BODY, objectMapper.readValue(body, JsonNode.class));
            } else {
                objectMsg.put(BODY, body);
            }
        } catch (IOException e1) { //never happen
            LoggerFactory.getLogger("RequestBody").error("can't get request body", e1);
        }
        return objectMsg;
    }

    private static JsonNode createLogMsg(HttpServletRequest request, int httpStatus, ErrorResponse errResp) {
        return createLogMsg(request, httpStatus)
                .set(RESPONSE, objectMapper.convertValue(errResp, JsonNode.class));
    }

    private static String getRequestBody(HttpServletRequest request) throws UnsupportedEncodingException {
        if (!(request instanceof ContentCachingRequestWrapper)) {
            return null;
        }
        byte[] bodyContent = ((ContentCachingRequestWrapper) request).getContentAsByteArray();
        if (bodyContent.length == 0) {
            return null;
        }
        return new String(bodyContent, request.getCharacterEncoding());
    }
}
