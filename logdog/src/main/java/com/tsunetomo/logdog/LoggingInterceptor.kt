package com.tsunetomo.logdog

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset

class LoggingInterceptor(private val tag: String = DEFAULT_TAG) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var charset = Charset.forName(UTF8)
        val request = chain.request()
        val requestBody = request.body
        val connection = chain.connection()
        val headers = request.headers

        val message = StringBuilder().append(START_REQ).append("\n")

        message.appendKeyValuePairs(
            KEY_METHOD to request.method,
            KEY_URL to request.url,
            KEY_CONNECTION to connection?.protocol(),
            KEY_CONTENT_LENGTH to requestBody?.contentLength()
        )

        message.appendKeyValue(
            KEY_HEADER,
            headers.joinToString(";") { "${it.first},${it.second}" }
        )

        val reqBodyBuf = Buffer()
        requestBody?.writeTo(reqBodyBuf)
        val reqContentType = requestBody?.contentType()
        if (reqContentType != null) {
            charset = reqContentType.charset(charset)
        }

        message.appendKeyValue(
            KEY_REQ_BODY,
            reqBodyBuf.readString(charset).replace("\n", "")
        )

        val startTime = System.nanoTime()
        message.appendKeyValue(KEY_START_TIME, startTime).append(END_REQ)
        Log.d(tag, message.toString())

        message.clear().append(START_RESP).append("\n").appendKeyValue(KEY_START_TIME, startTime)
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            message.appendKeyValue(KEY_HTTP_FAIL, e).append(END_RESP)
            Log.d(tag, message.toString())
            throw e
        }

        val requestTimeNano = System.nanoTime() - startTime
        message.appendKeyValuePairs(
            KEY_CODE to response.code,
            KEY_MESSAGE to response.message,
            KEY_TIME_TOOK to requestTimeNano
        )

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

        message.appendKeyValue(
            KEY_RESP_BODY,
            respBodyBuf?.clone()?.readString(charset)?.replace("\n", "")
        ).append(END_RESP)

        Log.d(tag, message.toString())
        return response
    }

    private fun StringBuilder.appendKeyValue(key: String, value: Any?): StringBuilder {
        return append("$key:::$value\n")
    }

    private fun StringBuilder.appendKeyValuePairs(
        vararg pairs: Pair<String, Any?>,
    ): StringBuilder = apply {
        pairs.forEach { (key, value) ->
            appendKeyValue(key, value)
        }
    }
}