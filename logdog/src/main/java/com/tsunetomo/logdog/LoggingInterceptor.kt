package com.tsunetomo.logdog

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class LoggingInterceptor : Interceptor {
    var tag = "Logdog"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body
        val connection = chain.connection()
        val headers = request.headers

        val message = StringBuilder()
        message
            .append("start_req")
            .append("\n")
            .append("method:::").append(request.method)
            .append("\n")
            .append("url:::").append(request.url)
            .append("\n")
            .append("connection:::").append(connection?.protocol())
            .append("\n")
            .append("content_length:::").append(requestBody?.contentLength())
            .append("\n")

        headers.forEachIndexed { index, header ->
            message.append("header $index:::").append(header).append("\n")
        }

        val startTime = System.nanoTime()
        message
            .append("body:::")
            .append(requestBody?.toString()?.replace("\n", ""))
            .append("\n")
            .append("start_time:::")
            .append(startTime)
            .append("\n")
            .append("end_req")

        Log.d(tag, message.toString())
        message.clear().append("start_resp:::").append(startTime).append("\n")
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            message
                .append("http_fail:::$e")
                .append("\n")
                .append("end_resp")
            Log.d(tag, message.toString())
            throw e
        }

        val requestTimeNano = System.nanoTime() - startTime
        message.append("time_took:::").append(requestTimeNano).append("\n")

        val responseBody = response.body
        response.headers.forEachIndexed { index, header ->
            message.append("header $index:::").append(header).append("\n")
        }

        message
            .append("response_body:::")
            .append(responseBody?.string()?.replace("\n", ""))
            .append("\n")
            .append("end_resp")

        Log.d(tag, message.toString())
        return response
    }
}