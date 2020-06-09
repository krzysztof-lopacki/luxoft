package com.luxoft.codingchallenge.utils.network

import android.os.Build
import android.util.Log
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

private val TLS_V12_ONLY = arrayOf("TLSv1.2")

/**
 * Fix for the pre-Lollipop devices which do not fully support all versions of TLS protocol.
 */
fun enableTls12OnPreLollipop(builder: OkHttpClient.Builder): OkHttpClient.Builder {
    if (Build.VERSION.SDK_INT in 19..21) {
        try {
            val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, null, null)
            builder.sslSocketFactory(Tls12SocketFactory(sslContext.socketFactory))
            val connectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2).build()
            val specs: MutableList<ConnectionSpec> = ArrayList()
            specs.add(connectionSpec)
            specs.add(ConnectionSpec.COMPATIBLE_TLS)
            specs.add(ConnectionSpec.CLEARTEXT)
            builder.connectionSpecs(specs)
        } catch (exc: Exception) {
            Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc)
        }
    }
    return builder
}

private class Tls12SocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {
    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites
    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return patchSocket(delegate.createSocket(s, host, port, autoClose));
    }

    override fun createSocket(host: String, port: Int): Socket {
        return patchSocket(delegate.createSocket(host, port));
    }

    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress,
        localPort: Int
    ): Socket {
        return patchSocket(delegate.createSocket(host, port, localHost, localPort));
    }

    override fun createSocket(host: InetAddress, port: Int): Socket {
        return patchSocket(delegate.createSocket(host, port));
    }

    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int
    ): Socket {
        return patchSocket(delegate.createSocket(address, port, localAddress, localPort));
    }

    private fun patchSocket(socket: Socket): Socket {
        if (socket is SSLSocket) {
            socket.enabledProtocols =
                TLS_V12_ONLY;
        }
        return socket;
    }
}