package com.linkedpipes.etl.storage.http;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * Allow multipart on selected request types, POST and PUT.
 */
class MultipartResolver extends CommonsMultipartResolver {

    private final static String POST = "POST";

    private final static String PUT = "PUT";

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        if (!supportBody(request)) {
            return false;
        }
        ServletRequestContext context = new ServletRequestContext(request);
        return FileUploadBase.isMultipartContent(context);
    }

    private boolean supportBody(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase();
        return POST.equals(method) || PUT.equals(method);
    }

}
