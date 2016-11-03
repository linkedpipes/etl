package com.linkedpipes.etl.storage.web;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * Custom implementation to enable support of Multipart on PUT.
 */
class MultipartResolver extends CommonsMultipartResolver {

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        // Enable POST and PUT method names.
        if (!"POST".equalsIgnoreCase(request.getMethod()) &&
                !"PUT".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        //
        return FileUploadBase.isMultipartContent(
                new ServletRequestContext(request));
    }

}
