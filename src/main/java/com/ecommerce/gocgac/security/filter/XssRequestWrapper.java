package com.ecommerce.gocgac.security.filter;

import com.ecommerce.gocgac.common.util.XssSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper để sanitize request parameters
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {
    
    private final Map<String, String[]> sanitizedParameters;
    
    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
        this.sanitizedParameters = new HashMap<>();
        sanitizeParameters(request);
    }
    
    private void sanitizeParameters(HttpServletRequest request) {
        Map<String, String[]> originalParameters = request.getParameterMap();
        
        for (Map.Entry<String, String[]> entry : originalParameters.entrySet()) {
            String[] values = entry.getValue();
            if (values != null) {
                String[] sanitizedValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    sanitizedValues[i] = XssSanitizer.sanitize(values[i]);
                }
                sanitizedParameters.put(entry.getKey(), sanitizedValues);
            }
        }
    }
    
    @Override
    public String getParameter(String name) {
        String[] values = sanitizedParameters.get(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }
    
    @Override
    public String[] getParameterValues(String name) {
        return sanitizedParameters.get(name);
    }
    
    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(sanitizedParameters);
    }
    
    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(sanitizedParameters.keySet());
    }
}

