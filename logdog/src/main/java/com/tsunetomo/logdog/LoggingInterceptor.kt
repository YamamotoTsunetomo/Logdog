package com.tsunetomo.logdog

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset

class LoggingInterceptor : Interceptor {
    var tag: String = "Logdog"

    override fun intercept(chain: Interceptor.Chain): Response {
        var charset = Charset.forName("UTF-8")
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

        for (i in 0 until headers.size) {
            val name = headers.name(i)
            message.append("header:::").append(name).append("\n")

        }

        val reqBodyBuf = Buffer()
        requestBody?.writeTo(reqBodyBuf)
        val reqContentType = requestBody?.contentType()
        if (reqContentType != null) {
            charset = reqContentType.charset(charset)
        }

        message.append("body:::")
            .append(reqBodyBuf.readString(charset).replace("\n", ""))
            .append("\n")

        val startTime = System.nanoTime()
        message
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
        val respBodyBuf = responseBody?.let {
            val source = it.source()
            source.request(Long.MAX_VALUE)
            source.buffer
        }

        val respContentType = responseBody?.contentType()
        if (respContentType != null) {
            charset = respContentType.charset(charset)
        }

        message
            .append("response_body:::")
            .append(respBodyBuf?.clone()?.readString(charset)?.replace("\n", ""))
            .append("\n")
            .append("end_resp")

        Log.d(tag, message.toString())
        return response
    }
}