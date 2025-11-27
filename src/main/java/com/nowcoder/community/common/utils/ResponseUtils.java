package com.nowcoder.community.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ResponseUtils {

    private  final static String RESPONSE_TYPE= "application/json;charset=utf-8";

    public static void result(HttpServletResponse response, int code, Object msg) throws IOException {
        response.setStatus(code);
        response.setContentType(RESPONSE_TYPE);
        PrintWriter writer = response.getWriter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        writer.write(objectMapper.writeValueAsString(msg));
    }

}